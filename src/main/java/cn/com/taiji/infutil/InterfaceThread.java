package cn.com.taiji.infutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.com.taiji.pad2.management.domain.InterfaceGl;
import cn.com.taiji.pad2.management.domain.InterfaceRepository;

@Service
public class InterfaceThread implements Runnable {

	@Autowired
	private InterfaceListenService infS;

//	@Autowired
//	private InterfaceRepository infR;

	@Value(value = "${interfaceTime}")
	private String interfaceTime;

	/**
	 * 接口监控线程,访问不通的更新状态为0,正常则改变状态为1
	 */
	public void run() {
		while (true) {
			// System.out.println("更新url状态");
			Map<String, List<InterfaceGl>> map = infS.interfaceListen();
			for (InterfaceGl interfaceGl : map.get("erro")) {
				infS.updateInf(interfaceGl, "0");
			}
			for (InterfaceGl interfaceGl : map.get("bingo")) {
				infS.updateInf(interfaceGl, "1");
			}
			try {
				Thread.sleep(Integer.parseInt(interfaceTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
