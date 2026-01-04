package com.sopze.mc.redstonepp;

public class Constants {

  public static final String MOD_ID_CONST= "redstonepp";

  public static final String
    MOD_ID, LOG_ID,
    LOC_MAIN,
    LOC_REGISTRY_OVERRIDE,
    ERR_VERSION_MALFORMED,
    ERR_VERSION_PARSING,
    OVERRIDE_BLOCK,
    CHECKING_CLIENT_MOD,
    VERSION_CHECK_INFO, VERSION_CHECK_MESSAGE, VERSION_CHECK_SUCCEED, VERSION_CHECK_FAIL;

  static{
    MOD_ID = "redstonepp"; LOG_ID = "Redstone++";

    LOC_MAIN= "onInitialize()";
    LOC_REGISTRY_OVERRIDE= "r_register";

    ERR_VERSION_MALFORMED= "Malformed mod version string: %s";
    ERR_VERSION_PARSING= "Error while parsing mod version digit[%d]: %s";

    OVERRIDE_BLOCK= "Overriding vanilla block: %s";
    CHECKING_CLIENT_MOD= "Checking mod version parity with %s";
    VERSION_CHECK_INFO= "Server version= v%s | Local version= v%s";
    VERSION_CHECK_MESSAGE= "Redstone++ client-server versions %s"; VERSION_CHECK_SUCCEED= "compatible. Happy rusting!"; VERSION_CHECK_FAIL= "incompatible, mod disabled on client";
  }
}
