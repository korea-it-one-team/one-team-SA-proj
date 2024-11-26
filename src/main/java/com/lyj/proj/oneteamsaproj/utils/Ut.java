package com.lyj.proj.oneteamsaproj.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Ut {

	public static String jsReplace(String msg, String replaceUri) {

		if (msg == null) {
			msg = "";
		}
		if (replaceUri == null) {
			replaceUri = "/";
		}

		return Ut.f("""
					<script>
						let msg = '%s';
						if(msg.length > 0){
							alert(msg);
						}
						location.replace('%s');
					</script>
				""", msg, replaceUri);
	}

	public static String jsReplace(String resultCode, String msg, String replaceUri) {

		if (resultCode == null) {
			resultCode = "";
		}
		if (msg == null) {
			msg = "";
		}
		if (replaceUri == null) {
			replaceUri = "/";
		}

		String resultMsg = resultCode + "/" + msg;

		return Ut.f("""
					<script>
						let resultMsg = '%s'.trim();

						if(resultMsg.length > 0){
							alert(resultMsg);
						}
						location.replace('%s');
					</script>
				""", resultMsg, replaceUri);
	}

	public static String jsReplace(String msg, String replaceUri, Object... args) {
		if (msg == null) {
			msg = "";
		}
		if (replaceUri == null) {
			replaceUri = "/";
		}

		// args가 포함된 replaceUri 구성
		String formattedUri = String.format(replaceUri, args);

		return Ut.f("""
				    <script>
				        let msg = '%s';
				        if (msg.length > 0) {
				            alert(msg);
				        }
				        location.replace('%s');
				    </script>
				""", msg, formattedUri);
	}

	public static String jsHistoryBack(String resultCode, String msg) {

		String resultMsg = resultCode + "/" + msg;

		return Ut.f("""
					<script>
						let resultMsg = '%s'.trim();

						if(resultMsg.length > 0){
							alert(resultMsg);
						}
						history.back();
					</script>
				""", resultMsg);
	}

	public static boolean isEmptyOrNull(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			return ((String) obj).trim().length() == 0;
		}

		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).isEmpty();
		}

		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}

		return false;
	}

	public static String f(String format, Object... args) {

		return String.format(format, args);
	}

	public static String getEncodedCurrentUri(String currentUri) {
		try {
			return URLEncoder.encode(currentUri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return currentUri;
		}
	}

	public static <T> T ifEmpty(T data, T defaultValue) {
		if (isEmpty(data)) {
			return defaultValue;
		}

		return data;
	}

	public static String getDateStrLater(long seconds) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String dateStr = format.format(System.currentTimeMillis() + seconds * 1000);

		return dateStr;
	}

	public static String toJsonStr(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String toJsonStr(Map<String, Object> param) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(param);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String getStrAttr(Map map, String attrName, String defaultValue) {
		if (map.containsKey(attrName)) {
			return (String) map.get(attrName);
		}

		return defaultValue;
	}

	public static String getFileExtTypeCodeFromFileName(String fileName) {
		String ext = getFileExtFromFileName(fileName).toLowerCase();

		switch (ext) {
			case "jpeg":
			case "jpg":
			case "gif":
			case "png":
				return "img";
			case "mp4":
			case "avi":
			case "mov":
				return "video";
			case "mp3":
				return "audio";
		}

		return "etc";
	}

	public static String getFileExtType2CodeFromFileName(String fileName) {
		String ext = getFileExtFromFileName(fileName).toLowerCase();

		switch (ext) {
			case "jpeg":
			case "jpg":
				return "jpg";
			case "gif":
				return ext;
			case "png":
				return ext;
			case "mp4":
				return ext;
			case "mov":
				return ext;
			case "avi":
				return ext;
			case "mp3":
				return ext;
		}

		return "etc";
	}

	public static String getFileExtFromFileName(String fileName) {
		int pos = fileName.lastIndexOf(".");
		String ext = fileName.substring(pos + 1);

		return ext;
	}

	public static String getNowYearMonthDateStr() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy_MM");

		String dateStr = format1.format(System.currentTimeMillis());

		return dateStr;
	}

	public static List<Integer> getListDividedBy(String str, String divideBy) {
		return Arrays.asList(str.split(divideBy)).stream().map(s -> Integer.parseInt(s.trim()))
				.collect(Collectors.toList());
	}

	public static boolean deleteFile(String filePath) {
		java.io.File ioFile = new java.io.File(filePath);
		if (ioFile.exists()) {
			return ioFile.delete();
		}

		return true;
	}

	public static Map<String, Object> mapOf(Object... args) {
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("인자를 짝수개 입력해주세요.");
		}

		int size = args.length / 2;

		Map<String, Object> map = new LinkedHashMap<>();

		for (int i = 0; i < size; i++) {
			int keyIndex = i * 2;
			int valueIndex = keyIndex + 1;

			String key;
			Object value;

			try {
				key = (String) args[keyIndex];
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("키는 String으로 입력해야 합니다. " + e.getMessage());
			}

			value = args[valueIndex];

			map.put(key, value);
		}

		return map;
	}

	public static int getAsInt(Object object, int defaultValue) {
		if (object instanceof BigInteger) {
			return ((BigInteger) object).intValue();
		} else if (object instanceof Double) {
			return (int) Math.floor((double) object);
		} else if (object instanceof Float) {
			return (int) Math.floor((float) object);
		} else if (object instanceof Long) {
			return (int) object;
		} else if (object instanceof Integer) {
			return (int) object;
		} else if (object instanceof String) {
			return Integer.parseInt((String) object);
		}

		return defaultValue;
	}

	public static <T> T ifNull(T data, T defaultValue) {
		return data != null ? data : defaultValue;
	}

	public static <T> T reqAttr(HttpServletRequest req, String attrName, T defaultValue) {
		return (T) ifNull(req.getAttribute(attrName), defaultValue);
	}

	public static Map<String, String> getParamMap(HttpServletRequest req) {
		Map<String, String> param = new HashMap<>();

		Enumeration<String> parameterNames = req.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			String paramValue = req.getParameter(paramName);

			param.put(paramName, paramValue);
		}

		return param;
	}

	public static String getEncodedUriComponent(String str) {
		if (str == null) {
			return "";
		}
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	public static String getTempPassword(int length) {
		int index = 0;
		char[] charArr = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
				'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; i++) {
			index = (int) (charArr.length * Math.random());
			sb.append(charArr[index]);
		}

		return sb.toString();
	}
}
