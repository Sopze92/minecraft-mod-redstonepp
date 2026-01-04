package com.sopze.mc.redstonepp;

import org.slf4j.LoggerFactory;

import static com.sopze.mc.redstonepp.Constants.LOG_ID;

public class Logger {
  private static final org.slf4j.Logger _LOGGER = LoggerFactory.getLogger(LOG_ID);

  private static final String _LOG_PREFIX, _LOG_PREFIX_FULL, _LOG_FORMAT_PREFIX, _LOG_FORMAT_PREFIX_FULL;
  static{
    _LOG_PREFIX = "@ %s :: ";
    _LOG_PREFIX_FULL = "(%s) " + _LOG_PREFIX;
    _LOG_FORMAT_PREFIX = _LOG_PREFIX + "%s";
    _LOG_FORMAT_PREFIX_FULL = _LOG_PREFIX_FULL + "%s";
  }

  public static void slog(String message){ _LOGGER.info(message); }
  public static void slog(String message, Object... params){ _LOGGER.info(String.format(message, params)); }
  public static void log(String location, String message){ _LOGGER.info(String.format(_LOG_FORMAT_PREFIX, location, message)); }
  public static void log(String location, String message, Object... params){ _LOGGER.info(String.format(String.format(_LOG_FORMAT_PREFIX, location, message), params)); }
  public static void slogDev(String message){ _LOGGER.debug(message); }
  public static void slogDev(String message, Object... params){ _LOGGER.debug(String.format(message, params)); }
  public static void logDev(String location, String message){ _LOGGER.debug(String.format(_LOG_FORMAT_PREFIX, location, message)); }
  public static void logDev(String location, String message, Object... params){ _LOGGER.debug(String.format(String.format(_LOG_FORMAT_PREFIX, location, message), params)); }
  public static void slogWrn(String message){ _LOGGER.warn(message); }
  public static void slogWrn(String message, Object... params){ _LOGGER.warn(String.format(message, params)); }
  public static void logWrn(String location, String message){ _LOGGER.warn(String.format(_LOG_FORMAT_PREFIX, location, message)); }
  public static void logWrn(String location, String message, Object... params){ _LOGGER.warn(String.format(String.format(_LOG_FORMAT_PREFIX, location, message), params)); }
  public static void slogErr(String message){ _LOGGER.error(message); }
  public static void slogErr(String message, Object... params){ _LOGGER.error(String.format(message, params)); }
  public static void logErr(String location, String message){ _LOGGER.error(String.format(_LOG_FORMAT_PREFIX, location, message)); }
  public static void logErr(String location, String message, Object... params){ _LOGGER.error(String.format(String.format(_LOG_FORMAT_PREFIX, location, message), params)); }

  public static String getModLogString(String location) { return String.format(_LOG_PREFIX_FULL, LOG_ID, location); }
  public static String getFullLogString(String location, String message){ return String.format(_LOG_FORMAT_PREFIX_FULL, LOG_ID, location, message); }
  public static String getFullLogString(String location, String message, Object... params){ return String.format(String.format(_LOG_FORMAT_PREFIX_FULL, LOG_ID, location, message), params); }
}
