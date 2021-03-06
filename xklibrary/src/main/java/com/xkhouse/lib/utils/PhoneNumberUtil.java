package com.xkhouse.lib.utils;

/**
 * 
 * @Author nieshuting
 * @ClassName PhoneNumberUtil
 * @Description 判断手机号码
 * @Date 2013-12-19上午11:20:44
 */
public class PhoneNumberUtil {
	/**
	 * 中国移动拥有号码段为:139,138,137,136,135,134,159,158,157(3G),152,151,150,188(3G),187(3G
	 * ),184(3G),183(3G),182(3G);17个号段 中国联通拥有号码段为:130,131,132,155,156(3G),186(3G),185(3G);7个号段
	 * 中国电信拥有号码段为:133,153,189(3G),180(3G),181(3G);5个号码段
	 */
	private static String regMobileStr = "^1(([3][456789])|([5][012789])|([8][23478]))[0-9]{8}$";
	private static String regMobile3GStr = "^((157)|(18[23478]))[0-9]{8}$";
	private static String regUnicomStr = "^1(([3][012])|([5][56])|([8][56]))[0-9]{8}$";
	private static String regUnicom3GStr = "^((156)|(18[56]))[0-9]{8}$";
	private static String regTelecomStr = "^1(([3][3])|([5][3])|([8][019]))[0-9]{8}$";
	private static String regTelocom3GStr = "^(18[019])[0-9]{8}$";
	private static String regPhoneString = "^(?:13\\d|15\\d)\\d{5}(\\d{3}|\\*{3})$";

	private String mobile = "";
	private int facilitatorType = 0;
	private boolean isLawful = false;
	private boolean is3G = false;

	public PhoneNumberUtil(String mobile) {
		this.setMobile(mobile);
	}

	public void setMobile(String mobile) {
		if (mobile == null) {
			return;
		}
		/** */
		/** 第一步判断中国移动 */
		if (mobile.matches(PhoneNumberUtil.regMobileStr)) {
			this.mobile = mobile;
			this.setFacilitatorType(0);
			this.setLawful(true);
			if (mobile.matches(PhoneNumberUtil.regMobile3GStr)) {
				this.setIs3G(true);
			}
		}
		/** */
		/** 第二步判断中国联通 */
		else if (mobile.matches(PhoneNumberUtil.regUnicomStr)) {
			this.mobile = mobile;
			this.setFacilitatorType(1);
			this.setLawful(true);
			if (mobile.matches(PhoneNumberUtil.regUnicom3GStr)) {
				this.setIs3G(true);
			}
		}
		/** */
		/** 第三步判断中国电信 */
		else if (mobile.matches(PhoneNumberUtil.regTelecomStr)) {
			this.mobile = mobile;
			this.setFacilitatorType(2);
			this.setLawful(true);
			if (mobile.matches(PhoneNumberUtil.regTelocom3GStr)) {
				this.setIs3G(true);
			}
		}
		/** */
		/** 第四步判断座机 */
		if (mobile.matches(PhoneNumberUtil.regPhoneString)) {
			this.mobile = mobile;
			this.setFacilitatorType(0);
			this.setLawful(true);
			if (mobile.matches(PhoneNumberUtil.regMobile3GStr)) {
				this.setIs3G(true);
			}
		}
	}

	public String getMobile() {
		return mobile;
	}

	public int getFacilitatorType() {
		return facilitatorType;
	}

	public boolean isLawful() {
		return isLawful;
	}

	public boolean isIs3G() {
		return is3G;
	}

	private void setFacilitatorType(int facilitatorType) {
		this.facilitatorType = facilitatorType;
	}

	private void setLawful(boolean isLawful) {
		this.isLawful = isLawful;
	}

	private void setIs3G(boolean is3G) {
		this.is3G = is3G;
	}

	/**
	 * 验证手机号码是否正确;
	 * 
	 * @param number
	 *            手机号码
	 * @return true为合法，否则非法；
	 */
	public static boolean checkPhoneNumber(String number) {
		if (number.length() == 11
				&& (number.startsWith("13") || number.startsWith("14")
						|| number.startsWith("15") || number.startsWith("16") || number
							.startsWith("18"))) {
			return true;
		} else {
			return false;
		}
	}

}
