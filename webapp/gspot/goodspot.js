var gspot = angular.module('gspot', ['ngResource', 'ui.map','ui.event']);

gspot.run(function($rootScope){
    $rootScope.mapHeight = "1000px";
    $rootScope.mapWidth = "100%";

});

var spotsResources = gspot.factory('spotsResources', function($resource){

//http://10.126.0.88:1080/whatever.json
	return $resource('http://:host/whatever.json?southWestLat=:southWestLat&southWestLng=:southWestLng&northEastLat=:northEastLat&northEastLng=:northEastLng', {
		southWestLat : '@southWestLat',
		southWestLng : '@southWestLng',
		northEastLat : '@northEastLat',
		northEastLng : '@northEastLng',
		host: '@host'
	}) 	
	
});

var mainCtrl = gspot.controller('mainCtrl',['$scope','spotsResources', function mainCtrl($scope, spotsResources) {
	$scope.markers = [];
	$scope.spots = [];
	$scope.isLoading = false;
	$scope.host = '10.126.0.88:1080';
	
	$scope.displayBoard = false;
    var singapore = new google.maps.LatLng(1.2485223959625258, 103.83118438720703);  
    
    $scope.mapOptions = {
        center: singapore,
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true
    };

    $scope.markerClicked = function(index) {
    	$scope.displaySpot = $scope.spots[index];
        $scope.displayBoard = true;
    };
    
    $scope.showSpots = function(){

    	$scope.spots = [];
    	$scope.isLoading = true;
    	
    	for(var i = 0; i < $scope.markers.length; i ++){
    		$scope.markers[i].setMap(null);
    	}
    	
    	$scope.markers = [];
    	
    	var southWestLat = $scope.map.getBounds().getSouthWest().lat();
    	var southWestLng = $scope.map.getBounds().getSouthWest().lng();
    	var northEastLat = $scope.map.getBounds().getNorthEast().lat();
    	var northEastLng = $scope.map.getBounds().getNorthEast().lng();
    
    	spotsResources.query({
			southWestLat : southWestLat,
			southWestLng : southWestLng,
			northEastLat : northEastLat,
			northEastLng : northEastLng,
			host : $scope.host
			},
			function(response){

    		$scope.spots = response;
    		
    		console.log($scope.spots);
    		
    		if($scope.spots.length === 0){
    			alert('No photos are found - try again!');
    		} else {
    		
    		for(var i = 0; i < $scope.spots.length ; i++){
    		//Add each spot a marker object
    		
    			var position = new google.maps.LatLng($scope.spots[i]['lat'], $scope.spots[i]['lng']);
    			
    			var marker = new google.maps.Marker({
    				map: $scope.map,
    				position: position
    			});
    			
    			$scope.markers[i] = marker;
    			
    		}
    		
    		$scope.isLoading = false;
    		}
    		
    	});
    	
    };
    
    $scope.showBounds = function(){
    	alert("SouthWest:" + $scope.map.getBounds().getSouthWest().toString());
    	alert("NorthEast:" + $scope.map.getBounds().getNorthEast().toString());
    }
    
}]);