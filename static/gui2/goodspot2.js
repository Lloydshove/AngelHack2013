var mainModule = angular.module('app.goodspot', ['ngResource', 'ui.map', 'ui.bootstrap'])
    .factory('DataService', ['$resource', function($resource){

        return $resource(':host/spots.json?' +
            'southWestLat=:southWestLat&southWestLng=:southWestLng&northEastLat=:northEastLat&northEastLng=:northEastLng',
            {host: '@host',
             southWestLat : '@southWestLat',
             southWestLng : '@southWestLng',
             northEastLat : '@northEastLat',
             northEastLng : '@northEastLng'})

    }])
    .factory('BroadcastService', ['$rootScope', function($rootScope){

        var broadcastService = {};

        broadcastService.alert = "";

        broadcastService.sendAlert = function(alertType, alertMessage){
            this.alert = {
                type: alertType,
                msg: alertMessage};
            this.broadcastAlert();
        }

        broadcastService.broadcastAlert = function(){
            $rootScope.$broadcast('handleAlert');
        }

        broadcastService.showProgressBar = function(){
            $rootScope.$broadcast('showProgressBar');
        }

        broadcastService.hideProgressBar = function(){
            $rootScope.$broadcast('hideProgressBar');
        }

        return broadcastService;

    }]);

var mapCtrl = mainModule.controller('MapCtrl', ['$scope', '$timeout' , '$modal', 'DataService', 'BroadcastService',
function($scope, $timeout, $modal, dataService, broadcastService){

    var toServerTask;
    $scope.review = false;
    $scope.host = "localhost";

    var singapore = new google.maps.LatLng(1.2485223959625258, 103.83118438720703);

    $scope.mapOptions = {
        center: singapore,
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true,
        clickableLabels:false
    };

    $scope.markerClicked = function(index){
        //TODO concrete coding for executing the display panel
        $scope.review = true;
        $scope.reviewSpot = $scope.spots[index];
    }

    //TODO change this function name to boundary change
    $scope.search = function() {

        if(toServerTask != null){
            console.log("cancel pending to server event!");
            $timeout.cancel(toServerTask);
            broadcastService.hideProgressBar();
        }
        //Disable loading bar status

        toServerTask = $timeout(function(){

            var southWestLat = $scope.map.getBounds().getSouthWest().lat();
            var southWestLng = $scope.map.getBounds().getSouthWest().lng();
            var northEastLat = $scope.map.getBounds().getNorthEast().lat();
            var northEastLng = $scope.map.getBounds().getNorthEast().lng();
            var searchCenter = $scope.map.getCenter();
            var zoomLevel = $scope.map.getZoom();

            console.log("start requesting data!");
            console.log("query host:"+$scope.host);

            broadcastService.showProgressBar();
            dataService.query({
                host: $scope.host,
                southWestLat : southWestLat,
                southWestLng : southWestLng,
                northEastLat : northEastLat,
                northEastLng : northEastLng
            }, function(data){
//
//                if(southWestLat != $scope.map.getBounds().getSouthWest().lat() &&
//                    southWestLng != $scope.map.getBounds().getSouthWest().lng() &&
//                    northEastLat != $scope.map.getBounds().getNorthEast().lat() &&
//                    northEastLng != $scope.map.getBounds().getNorthEast().lng()
//                ){
//                    console.log("Boundaries changed. Ignore response");
//                }else{

                    //console.log("get data from resources: "+ data);
                    broadcastService.hideProgressBar();
                    if(data == [] || data == null || data == {} || data == ""){
                        broadcastService.sendAlert("error", "No spot can be found in this boundary, try other region!");
                    }else{
                        //TODO appending data to the map and add marks
                        if($scope.spots != null){
                            for(var i = 0; i < $scope.spots.length; i ++){
                                $scope.spots[i].marker.setMap(null);
                            }
                        }

                        $scope.spots = [];
                        $scope.spots = data;

                        for (var i = 0; i < $scope.spots.length; i++){
                            var position = new google.maps.LatLng($scope.spots[i]['lat'], $scope.spots[i]['lng']);
                            var marker = new google.maps.Marker({
                                map: $scope.map,
                                position: position
                            });
                            $scope.spots[i].marker = marker;
                        }

                        broadcastService.sendAlert("success", "Please click on any marker to see details!");
                        $scope.map.setZoom(zoomLevel);
                        $scope.map.panTo(searchCenter);

                    }

//                }

            }, function(error){
//                if(southWestLat != $scope.map.getBounds().getSouthWest().lat() &&
//                    southWestLng != $scope.map.getBounds().getSouthWest().lng() &&
//                    northEastLat != $scope.map.getBounds().getNorthEast().lat() &&
//                    northEastLng != $scope.map.getBounds().getNorthEast().lng()
//                ){
//
//                    console.log("Boundaries changed. Ignore response");
//
//                }else{
                    console.log("get data error out");
                    broadcastService.hideProgressBar();
                    broadcastService.sendAlert("error","Error when requesting the server!");
//                }

            });

        },0);

    }

}])

var alertCtrl = mainModule.controller('AlertCtrl',['$scope', '$timeout', 'BroadcastService' ,function($scope, $timeout, BroadcastService){

  $scope.alerts = [];

  $scope.closeAlert = function(index) {
    $scope.alerts.splice(index, 1);
  };

  $scope.$on('handleAlert', function(){
    $scope.alerts.push(BroadcastService.alert);

    $timeout(function(){
        $scope.closeAlert(0);
    },8000);

  })

}])

var progressBarCtrl = mainModule.controller('ProgressBarCtrl',['$scope', 'BroadcastService' ,function($scope, BroadcastService){

  $scope.$on('showProgressBar', function(){
    $scope.showProgressBar = true;
  })

  $scope.$on('hideProgressBar', function(){
    $scope.showProgressBar = false;
  })

}])

function onGoogleReady() {
  angular.bootstrap(document.getElementById("map"), ['app.goodspot']);
}