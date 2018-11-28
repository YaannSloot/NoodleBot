package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip.LookupService;

import main.IanSloat.thiccbot.BotUtils;

public class GeoLocator {

	private final Logger logger = LoggerFactory.getLogger(GeoLocator.class);
	private File geoDatabase = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "resources"
			+ BotUtils.PATH_SEPARATOR + "geoLiteCity" + BotUtils.PATH_SEPARATOR + "GeoLiteCity.dat");
	private File geoResourceDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "resources"
			+ BotUtils.PATH_SEPARATOR + "geoLiteCity");
	private String ipAddress;
	private LookupService lookup;
	private boolean wasLoadedSuccessfully = false;

	public GeoLocator(String ipAddress) {
		this.ipAddress = ipAddress;
		if (!(geoResourceDir.exists())) {
			geoResourceDir.mkdirs();
			logger.info("Directory \"" + geoResourceDir.getAbsolutePath()
					+ "\" was not found. A new directory was created");
		}
		if (!(geoDatabase.exists())) {
			logger.info("GeoLite Database file \"" + geoDatabase.getAbsolutePath()
					+ "\" was not found. Downloading database...");
			try {
				File archive = new File(geoDatabase + ".gz");
				FileUtils.copyURLToFile(
						new URL("http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz"), archive);
				logger.info("File \"GeoLiteCity.dat.gz\" downloaded successfully. Unzipping archive...");
				byte[] buffer = new byte[1024];
				try {
					GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(archive.getAbsolutePath()));
					FileOutputStream out = new FileOutputStream(geoDatabase.getAbsolutePath());
					int len;
					while ((len = gzis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					gzis.close();
					out.close();
					logger.info("Done");
				} catch (IOException e) {
					logger.error("File extraction attempt failed");
				}
				archive.delete();
			} catch (IOException e) {
				logger.error("download attempt failed");
				e.printStackTrace();
			}
		}
		if (geoDatabase.exists()) {
			logger.info("Loading GeoLiteCity database file...");
			try {
				lookup = new LookupService(geoDatabase.getAbsolutePath(),
						LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);
				wasLoadedSuccessfully = true;
				logger.info("Done");
				try {
					getCity();
					getCountry();
					getRegion();
					logger.info("Done.");
				} catch (java.lang.NullPointerException e) {
					logger.error("The ip address entered is either invalid or not a public address");
					wasLoadedSuccessfully = false;
				}
			} catch (IOException e) {
				logger.error("Load database failed");
			}
		} else
			logger.error("The geo database object failed to initialize");
	}

	public String getCity() {
		if (wasLoadedSuccessfully == true) {
			return lookup.getLocation(ipAddress).city;
		} else {
			logger.warn("The locator object did not initialize properly so no information was returned");
			return null;
		}
	}

	public String getRegion() {
		if (wasLoadedSuccessfully == true) {
			return lookup.getLocation(ipAddress).region;
		} else {
			logger.warn("The locator object did not initialize properly so no information was returned");
			return null;
		}
	}

	public String getCountry() {
		if (wasLoadedSuccessfully == true) {
			return lookup.getLocation(ipAddress).countryName;
		} else {
			logger.warn("The locator object did not initialize properly so no information was returned");
			return null;
		}
	}

	public String getIPAddress() {
		return this.ipAddress;
	}

}
