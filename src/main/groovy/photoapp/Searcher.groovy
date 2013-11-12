package photoapp

import com.aetrion.flickr.Flickr
import com.aetrion.flickr.FlickrException
import com.aetrion.flickr.REST
import com.aetrion.flickr.RequestContext
import com.aetrion.flickr.auth.Auth
import com.aetrion.flickr.auth.AuthInterface
import com.aetrion.flickr.auth.Permission
import com.aetrion.flickr.photos.GeoData
import com.aetrion.flickr.photos.Photo
import com.aetrion.flickr.photos.PhotoList
import com.aetrion.flickr.photos.SearchParameters

def AuthExample() {

    def API_KEY = "8427550a6678894dfe644455025bcf3a"
    def SECRET = "7ceb5bd318eb1120"


    f = new Flickr(
            API_KEY,
            SECRET,
            new REST())

    Flickr.debugStream = false;
    requestContext = RequestContext.getRequestContext();
    AuthInterface authInterface = f.getAuthInterface();
    def frob = authInterface.getFrob();
    System.out.println("frob: " + frob);
    URL url = authInterface.buildAuthenticationUrl(Permission.DELETE, frob);
    System.out.println("Press return after you granted access at this URL:");
    System.out.println(url.toExternalForm());
    BufferedReader infile =
        new BufferedReader(new InputStreamReader(System.in));
    String line = infile.readLine();
    try {
        Auth auth = authInterface.getToken(frob);
        System.out.println("Authentication success");
        // This token can be used until the user revokes it.
        System.out.println("Token: " + auth.getToken());
        System.out.println("nsid: " + auth.getUser().getId());
        System.out.println("Realname: " + auth.getUser().getRealName());
        System.out.println("Username: " + auth.getUser().getUsername());
        System.out.println("Permission: " + auth.getPermission().getType());
    } catch (FlickrException e) {
        System.out.println("Authentication failed");
        e.printStackTrace();
    }
}

//AuthExample()

def API_KEY = "8427550a6678894dfe644455025bcf3a"
def SECRET = "7ceb5bd318eb1120"
def TOKEN = "72157636711948485-ce10bb434213e7a1"

flickr = new Flickr(
        API_KEY,
        SECRET,
        new REST()
);

requestContext = RequestContext.getRequestContext();
Auth auth = new Auth();
auth.setPermission(Permission.READ);
auth.setToken(TOKEN);
requestContext.setAuth(auth);
Flickr.debugRequest = false;
Flickr.debugStream = false;


def startDate = Date.parse("dd-MM-yyyy", "29-08-2013")
def endDate = Date.parse("dd-MM-yyyy", "29-09-2013")
def photoInterface = flickr.getPhotosInterface()
def parameters = new SearchParameters()
parameters.hasGeo = 1
parameters.minUploadDate = startDate
parameters.maxUploadDate = endDate
//parameters.tags = ["food"]
//parameters.maxUploadDate = endDate
//parameters.placeId = "XAX6n_BTUb7y_QAw9w"
//parameters.accuracy=5
//parameters.accuracy = 10
def startLong = 103.607314
def startLat = 1.223625
def endLat = 1.498489
def endLong = 104.045102
def gridWidth = 40
def longDelta = (endLong - startLong)/gridWidth
def latDelta = (endLat - startLat)/gridWidth


//PhotoList photos = photoInterface.getWithGeoData(startDate, endDate, startDate, endDate, 1, "", [] as Set, 5, 1)
def perPage = 250

def lats = startLat..endLat in latDelta
def longs = startLong..endLong in longDelta

for (longStep in 0..gridWidth) {
    for (latStep in 0..gridWidth) {
        def output = ""
        def box = [(startLong + (longDelta * longStep)).toString(),
                (startLat + (latDelta * latStep)).toString(),
                (startLong + (longDelta * (longStep + 1))).toString(),
                (startLat + (latDelta * (latStep + 1))).toString()]
        parameters.setBBox(box)


        println "- - - - - - STARTED - - - - - - - "
        println box + longDelta
        for (pageNum in 1..8) {
            try {
                PhotoList photos = photoInterface.search(parameters, perPage, pageNum)

                println "we have " + photos.size() + " photos "

                def geoInterface = flickr.getGeoInterface()

                photos.each {
                    try {
                        Photo phot = (it as Photo)

                        def json = buildJson(phot, geoInterface.getLocation(phot.getId()))
                        println json
                        output += json + "\n"

                    } catch (Exception e) {
                        println(e)
                    }
                }

                if (photos.size() < perPage - 1) {
                    break
                }
            } catch (Exception e) {
                println(e)
            }
        }

        println "- - - - - - FINISHED - - - - - - - "
        if (output.size() > 0) {
            new File("""../../../../generated_data_20_sing_1RealMonth2/photos-flickr-singapore-${(startLong + (longDelta * longStep)).toString()}-${(startLat + (latDelta * latStep)).toString()}-${(startLong + (longDelta * (longStep + 1))).toString()}-${(startLat + (latDelta * (latStep + 1))).toString()}.json""").withWriter {
                out -> out.write(output)
            }
        }
    }
}

def buildJson(Photo photo, GeoData geo) {
    """{"id": "${photo.id}","server": "${photo.server}","farm": "${photo.farm}","userId": "${photo.owner.id}","secret": "${photo.secret}","title": "${photo.title.replaceAll('"', "'")}","loc": {"type": "Point","coordinates": [${geo.longitude},${geo.latitude}]}} """
}

