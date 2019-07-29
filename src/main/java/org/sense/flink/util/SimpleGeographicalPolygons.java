package org.sense.flink.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.java.tuple.Tuple3;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.URLs;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Contains;
import org.opengis.referencing.operation.TransformException;
import org.sense.flink.pojo.Point;

public class SimpleGeographicalPolygons {
	private File geoJSON;
	private DataStore dataStore;
	private FilterFactory2 filerFactory2;
	private SimpleFeatureSource simpleFeatureSource;
	private CRSCoordinateTransformer crsCoordinateTransformer;

	/**
	 * The Valencia GeoJSON file comes on the format urn:ogc:def:crs:OGC:1.3:CRS84,
	 * which is equivalent to the format EPSG:4326 on GeoTools
	 */
	public SimpleGeographicalPolygons() {
		this(new File("resources/valencia/admin_level_9_Valencia_polygons.geojson"));
	}

	public SimpleGeographicalPolygons(File geoJSON) {
		this.geoJSON = geoJSON;
		this.createDataStore();
		this.crsCoordinateTransformer = new CRSCoordinateTransformer();
	}

	private void createDataStore() {
		try {
			if (!geoJSON.exists()) {
				throw new Exception("GeoJSON file does not exits!");
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(GeoJSONDataStoreFactory.URLP.key, URLs.fileToUrl(geoJSON));
			dataStore = DataStoreFinder.getDataStore(params);
			filerFactory2 = CommonFactoryFinder.getFilterFactory2();
			simpleFeatureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Long getOsmId(Point point) {
		try {
			Point pointLonLat = null;
			if (point.getCsr().equals(CRSCoordinateTransformer.DEFAULT_CRS_TARGET)) {
				pointLonLat = point;
			} else {
				pointLonLat = crsCoordinateTransformer.xyToLonLatPoint(point.getX(), point.getY());
			}
			Contains contains = filerFactory2.contains(
					filerFactory2.property(simpleFeatureSource.getSchema().getGeometryDescriptor().getLocalName()),
					filerFactory2.literal(pointLonLat.toString()));
			SimpleFeatureCollection collection = simpleFeatureSource.getFeatures(contains);
			if (collection.size() > 0) {
				try (SimpleFeatureIterator itr = collection.features()) {
					while (itr.hasNext()) {
						SimpleFeature simpleFeature = itr.next();
						return Long.valueOf(simpleFeature.getAttribute("osm_id").toString());
					}
				}
			}
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Tuple3<Long, Long, String> getAdminLevel(Point point) {
		try {
			Point pointLonLat = null;
			if (point.getCsr().equals(CRSCoordinateTransformer.DEFAULT_CRS_TARGET)) {
				pointLonLat = point;
			} else {
				pointLonLat = crsCoordinateTransformer.xyToLonLatPoint(point.getX(), point.getY());
			}
			Contains contains = filerFactory2.contains(
					filerFactory2.property(simpleFeatureSource.getSchema().getGeometryDescriptor().getLocalName()),
					filerFactory2.literal(pointLonLat.toString()));
			SimpleFeatureCollection collection = simpleFeatureSource.getFeatures(contains);
			if (collection.size() > 0) {
				try (SimpleFeatureIterator itr = collection.features()) {
					while (itr.hasNext()) {
						SimpleFeature simpleFeature = itr.next();
						String districtName = simpleFeature.getAttribute("name").toString();
						Long adminLevel = Long.valueOf(simpleFeature.getAttribute("admin_level").toString());
						Long districtId = Long.valueOf(ValenciaDistricts.getDistrictId(districtName));
						return Tuple3.of(districtId, adminLevel, districtName);
					}
				}
			}
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Long getAdminLevelId(Point point) {
		try {
			Point pointLonLat = null;
			if (point.getCsr().equals(CRSCoordinateTransformer.DEFAULT_CRS_TARGET)) {
				pointLonLat = point;
			} else {
				pointLonLat = crsCoordinateTransformer.xyToLonLatPoint(point.getX(), point.getY());
			}
			Contains contains = filerFactory2.contains(
					filerFactory2.property(simpleFeatureSource.getSchema().getGeometryDescriptor().getLocalName()),
					filerFactory2.literal(pointLonLat.toString()));
			SimpleFeatureCollection collection = simpleFeatureSource.getFeatures(contains);
			if (collection.size() > 0) {
				try (SimpleFeatureIterator itr = collection.features()) {
					while (itr.hasNext()) {
						SimpleFeature simpleFeature = itr.next();
						String adminLevelName = simpleFeature.getAttribute("name").toString();
						return Long.valueOf(ValenciaDistricts.getDistrictId(adminLevelName));
					}
				}
			}
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean contains(Point point) {
		if (getOsmId(point) != null) {
			return true;
		}
		return false;
	}

	public void printProperties(List<Point> points) {
		try {
			for (Point point : points) {
				Point pointLonLat = null;
				if (point.getCsr().equals(CRSCoordinateTransformer.DEFAULT_CRS_TARGET)) {
					pointLonLat = point;
				} else {
					pointLonLat = crsCoordinateTransformer.xyToLonLatPoint(point.getX(), point.getY());
				}
				Contains contains = filerFactory2.contains(
						filerFactory2.property(simpleFeatureSource.getSchema().getGeometryDescriptor().getLocalName()),
						filerFactory2.literal(pointLonLat.toString()));
				SimpleFeatureCollection collection = simpleFeatureSource.getFeatures(contains);
				if (collection.size() > 0) {
					System.out.println("Found point [" + pointLonLat.toString() + "] on the file ["
							+ geoJSON.getAbsolutePath() + "]");
					try (SimpleFeatureIterator itr = collection.features()) {
						while (itr.hasNext()) {
							SimpleFeature simpleFeature = itr.next();
							Iterator<Property> properties = simpleFeature.getProperties().iterator();
							while (properties.hasNext()) {
								Property property = properties.next();
								System.out.println(
										property.getName() + " : " + simpleFeature.getAttribute(property.getName()));
							}
						}
					}
				}
			}
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SimpleGeographicalPolygons sgp = new SimpleGeographicalPolygons();
		List<Point> points = new ArrayList<Point>();
		points.add(new Point(-0.3630, 39.4477, CRSCoordinateTransformer.DEFAULT_CRS_TARGET));
		points.add(new Point(-0.3774, 39.4698, CRSCoordinateTransformer.DEFAULT_CRS_TARGET));

		points.add(new Point(727883.536, 4373590.846, CRSCoordinateTransformer.DEFAULT_CRS_SOURCE));
		points.add(new Point(727914.834, 4373625.414, CRSCoordinateTransformer.DEFAULT_CRS_SOURCE));
		sgp.printProperties(points);
	}
}
