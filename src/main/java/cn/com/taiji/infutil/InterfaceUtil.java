package cn.com.taiji.infutil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig.Interface;

import cn.com.taiji.pad2.management.domain.InterfaceGl;

/**
 * 接口监听服务
 * 
 * @author 林傲
 * @date 2017-08-14
 */
public class InterfaceUtil {

	/**
	 * 访问接口是否通
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public static int testWsdlConnection(String address) throws Exception {
		int status = 404;
		try {
			URL urlObj = new URL(address);
			HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
			oc.setUseCaches(false);
			oc.setConnectTimeout(3000); // 设置超时时间
			status = oc.getResponseCode();// 请求状态
			if (200 == status) {
				// 200是请求地址顺利连通。。
				return status;
			}
		} catch (Exception e) {
			throw e;
		}
		return status;
	}

	public static void main(String[] args) {
		InterfaceUtil fu = new InterfaceUtil();
		try {
			fu.testWsdlConnection("http://www.baidu.com");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
