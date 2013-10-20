package photoapp;import java.io.IOException;import java.util.List;import java.util.Random;import javax.servlet.ServletException;import javax.servlet.http.*;import org.codehaus.jettison.json.JSONArray;import org.codehaus.jettison.json.JSONException;import org.codehaus.jettison.json.JSONObject;import org.eclipse.jetty.server.Handler;import org.eclipse.jetty.server.Request;import org.eclipse.jetty.server.handler.AbstractHandler;import org.eclipse.jetty.server.nio.NetworkTrafficSelectChannelConnector;import org.eclipse.jetty.server.handler.HandlerList;import org.eclipse.jetty.server.handler.ResourceHandler;import org.eclipse.jetty.servlet.*;import static java.lang.Double.parseDouble;public class Server{    public static void main(String[] args) throws Exception{        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(1080);        RequestHandler context = new RequestHandler();        NetworkTrafficSelectChannelConnector connector = new NetworkTrafficSelectChannelConnector(server);        connector.setPort(8080);        server.addConnector(connector);        ResourceHandler resource_handler = new ResourceHandler();        resource_handler.setDirectoriesListed(true);        resource_handler.setWelcomeFiles(new String[]{ "index.html" });        resource_handler.setResourceBase("./gspot/");        HandlerList handlers = new HandlerList();        handlers.setHandlers(new Handler[] { resource_handler, context });        server.setHandler(handlers);        server.start();        server.join();       }    public static class RequestHandler extends AbstractHandler    {        @Override        public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {            response.setContentType("text/html;charset=utf-8");            response.setStatus(HttpServletResponse.SC_OK);            response.addHeader("Access-Control-Allow-Origin", "*");            response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");            Double swLat = parseDouble(request.getParameter("southWestLat"));            Double swLon = parseDouble(request.getParameter("southWestLng"));            Double neLat = parseDouble(request.getParameter("northEastLat"));            Double neLon = parseDouble(request.getParameter("northEastLng"));            double[][] mongoSearch = new double[][]{{swLon, swLat}, {neLon, neLat}};            List<Spot> result = DataManager.getSpots(mongoSearch);            String resultStr = "[";            for(Spot spot : result){                try {                    JSONArray json = new JSONArray(spot.getPhotos());                    resultStr += "{" +                        "\"count\": "+spot.getCount()+ "," +                        "\"lat\":" + spot.getY() + "," +                        "\"lng\":"+ spot.getX() +"," +                        "\"imgs\": " +                            buildPhotoUrls(json) +                        "},";                } catch (JSONException e) {                    throw new RuntimeException(e);                }            }            if(resultStr.length() > 1){                resultStr = resultStr.substring(0, resultStr.length() - 1);            }            resultStr += "]";            response.getWriter().println(resultStr);            baseRequest.setHandled(true);            System.out.println(resultStr);        }        private String buildPhotoUrls(JSONArray json) throws JSONException {            String result="[";            for(int i = 0; i<json.length(); i ++){                JSONObject photoJson = json.getJSONObject(i);                result += toUrl(photoJson.getString("farm"), photoJson.getString("server"), photoJson.getString("id"), photoJson.getString("secret"))+ ",";            }            result = result.substring(0, result.length() - 1);            result += "]";            return result;        }        private void fakeData(HttpServletResponse response, Double swLat, Double swLon, Double neLat, Double neLon) throws IOException {            response.getWriter().println(                    "[" +                            randomPoint(swLat, swLon, neLat, neLon) + "," +                            randomPoint(swLat, swLon, neLat, neLon) + "," +                            randomPoint(swLat, swLon, neLat, neLon) + "," +                            randomPoint(swLat, swLon, neLat, neLon) + "," +                            randomPoint(swLat, swLon, neLat, neLon) + "]"            );        }        private String randomPoint(Double swLat, Double swLon, Double neLat, Double neLon) {            Random rand = new Random();            Double newLat = swLat + (rand.nextDouble() * (neLat-swLat));            Double newLon = swLon + (rand.nextDouble() * (neLon-swLon));            return "{" +                    "\"lat\":" + newLat + "," +                            "\"lng\":"+ newLon +"," +                            "\"imgs\": [ " +                            toUrl("3", "2857", "9034939815", "46015f831b")+ "," +                            toUrl("6", "5511", "9037160796", "fd7c3de6a2")+ "," +                            toUrl("4", "3786", "9034940627", "74a1a7bcca")+ "," +                            toUrl("6", "5349", "9037161474", "6e8b88d165")+                            "]" +                            "}";        }    }    public static String toUrl(String farmId, String serverId, String id, String secret){        return "\"http://farm"+farmId+".staticflickr.com/"+ serverId + "/" + id + "_" + secret + ".jpg\"";    }}