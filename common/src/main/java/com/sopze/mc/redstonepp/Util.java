package com.sopze.mc.redstonepp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sopze.mc.redstonepp.Constants.*;

public class Util {

  private static final Pattern _regexVersion= Pattern.compile("([\\d.]+)([A-Z])-([\\d.]+)");
  private static final String _releaseModes= "fbax";
  private static final byte[] _versionFallback= {0,0,0,3};

  protected static byte[] _computeVersionBytes(String version) {
    Matcher m = _regexVersion.matcher(version);
    if(m.find()){
      try {
        String[] d = m.group(1).split("\\.");
        byte[] v = _versionFallback.clone();
        for (int i = 0; i < 3; i++) {
          try { v[i] = Byte.parseByte(d[i]); }
          catch (Exception __) {
            Logger.logErr(LOC_MAIN, ERR_VERSION_PARSING, i, version);
            return _versionFallback;
          }
        }
        v[3] = (byte) _releaseModes.indexOf(m.group(2).toLowerCase().charAt(0));
        if(v[3] == -1) v[3]= 3;
        return v;
      }
      catch (Exception __) { Logger.logErr(LOC_MAIN, ERR_VERSION_MALFORMED, version); }
    }
    return _versionFallback;
  }

  protected static boolean _isValidVersion() { return MainCommon.getLocalVersion()[3] != 3; }

  public static boolean isCompatibleVersion(byte[] other) {
    // by now this is hardcoded
    byte[] lv= MainCommon.getLocalVersion();
    if(other[0]!= lv[0]) return false;
    if(other[1]!= lv[1]) return false;
    if(other[2]> lv[2]) return false;
    return true;
  }
}
