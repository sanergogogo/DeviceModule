package com.cocos.game;

public class GlobalConfig {

	// 渠道id
	public static String ChannelId = "channelId";

	// 以下配置有变化，在lib_sdkmgr的build.gradle中开启或关闭相关sdk的引入

	// 是否使用facebook
	public static boolean HasFacebook = true;

	// 是否使用firebase
	public static boolean HasFirebase = true;

	// 是否使用appsflyer
	public static boolean HasAppsFlyer = true;
	// appsflyer
	public static String AppsFlyerKey = "6Z7sPcPLx9cz6d3Vc3d4AQ";

	// 是否使用adjust
	public static boolean HasAdjust = true;
	// adjust
	public static String AdjustKey = "4zf1u1v99thc";

	// 是否使用google服务
	public static boolean HasGoogleService = true;

}