package org.fao.sola.clients.android.opentenure.tools;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.maps.Constants;
import org.fao.sola.clients.android.opentenure.maps.WKTWriter;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Various GIS functions
 */

public class GisUtility {
    /**
     * Returns list of vertices (LatLng) from provided polygon in WKT format
     * @param wktPolygon Polygon in WKT format
     * @return
     */
    public static List<LatLng> getVertices(String wktPolygon){
        List<LatLng> vertices = new ArrayList<>();
        if(StringUtility.isEmpty(wktPolygon)){
            return vertices;
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);

        Polygon polygon = null;

        try {
            polygon = (Polygon) reader.read(wktPolygon);
            polygon.setSRID(Constants.SRID);
        } catch (ParseException e) {
            Log.e(GisUtility.class.getName(), "Exception: " + e.getLocalizedMessage()
                    + " while parsing WKT " + wktPolygon);
            return null;
        }

        for (int i = 0; i < polygon.getExteriorRing().getNumPoints() - 1; i++) {
            Coordinate coord = polygon.getExteriorRing().getCoordinates()[i];
            vertices.add(new LatLng(coord.getOrdinate(Coordinate.Y), coord.getOrdinate(Coordinate.X)));
        }

        return vertices;
    }

    public static Geometry getGeomFromVertices(List<LatLng> vertices) {
        if (vertices == null || vertices.size() == 0) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordinates;
        Geometry shell;

        int i = 0;
        switch(vertices.size()){
            case 1:
                shell = gf.createPoint(new Coordinate(vertices.get(0).longitude, vertices.get(0).latitude));
                break;
            case 2:
                coordinates = new Coordinate[vertices.size()];
                for (LatLng vertex : vertices) {
                    coordinates[i] = new Coordinate(vertex.longitude, vertex.latitude);
                    i++;
                }
                shell = gf.createLineString(coordinates);
                break;
            default:
                coordinates = new Coordinate[vertices.size() + 1];

                for (LatLng vertex : vertices) {
                    coordinates[i] = new Coordinate(vertex.longitude, vertex.latitude);
                    i++;
                }
                coordinates[i] = new Coordinate(vertices.get(0).longitude, vertices.get(0).latitude);

                shell = gf.createLinearRing(coordinates);
                break;
        }
        shell.setSRID(Constants.SRID);
        return shell;
    }

    public static String getWktPolygonFromVertices(List<LatLng> vertices) throws Exception {
        if (vertices == null || vertices.size() == 0) {
            return null;
        }

        if(vertices.size() < 3){
            throw new Exception(OpenTenureApplication.getContext().getResources().getString(R.string.polygon_not_enough_points));
        }

        GeometryFactory gf = new GeometryFactory();
        WKTWriter wktWriter = new WKTWriter();

        Coordinate[] coordinates;
        Geometry geom;

        coordinates = new Coordinate[vertices.size() + 1];

        for (int i = 0; i < vertices.size(); i++) {
            coordinates[i] = new Coordinate(vertices.get(i).longitude, vertices.get(i).latitude);
        }

        coordinates[vertices.size()] = new Coordinate(vertices.get(0).longitude, vertices.get(0).latitude);
        geom = gf.createPolygon(coordinates);

        geom.setSRID(Constants.SRID);
        return wktWriter.write(geom);
    }
}
