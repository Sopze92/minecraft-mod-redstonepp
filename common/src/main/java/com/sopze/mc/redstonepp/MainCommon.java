package com.sopze.mc.redstonepp;

import com.sopze.mc.redstonepp.wrapper.I_LoaderWrapper;

public class MainCommon {

  private static I_LoaderWrapper _WRAPPER;

  public static String _LOCAL_VERSION_STRING;
  public static byte[] _LOCAL_VERSION;

  public static boolean
    _MOD_ENABLED_LOCALLY = false;

	public static void initialize(I_LoaderWrapper wrapper) {
    _WRAPPER= wrapper;

    _LOCAL_VERSION_STRING= _WRAPPER.computeVersionString();
    _LOCAL_VERSION= Util._computeVersionBytes(_LOCAL_VERSION_STRING);

    setEnabledLocally(Util._isValidVersion());
	}

  public static boolean isEnabledLocally(){ return _MOD_ENABLED_LOCALLY; }
  public static void setEnabledLocally(boolean state){ _MOD_ENABLED_LOCALLY = state; }

  public static String getLocalVersionString() { return _LOCAL_VERSION_STRING; }
  public static byte[] getLocalVersion() { return _LOCAL_VERSION; }

  // generic exception

  public static class ModExceptionSimple extends RuntimeException{
    public ModExceptionSimple(String message) { super(message); }
  }

}