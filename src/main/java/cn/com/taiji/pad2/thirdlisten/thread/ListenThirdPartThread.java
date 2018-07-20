package cn.com.taiji.pad2.thirdlisten.thread;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.com.taiji.pad2.thirdlisten.domain.Thirdlisten;
import cn.com.taiji.pad2.thirdlisten.domain.ThirdlistenRespository;
//import cn.com.taiji.pad2.thirdlisten.log.InterfaceLogService;

/**
 * 第三方服务访问本服务接口监听线程
 * 
 * @author Administrator
 *
 */
@Service
public class ListenThirdPartThread implements Runnable {

	private Thirdlisten thirdListenBean;

	private ThirdlistenRespository thirdlistenRespository;

	// private InterfaceLogService interfaceService;

	private String interfaceTime;

	@Override
	public void run() {
		while (true) {
			List<Thirdlisten> thidList = thirdlistenRespository.findAll();
			for (Thirdlisten thirdlisten : thidList) {
				thirdListenBean = thirdlisten;
				thirdListenBean = thirdlistenRespository.findOne(thirdListenBean.getTid());
				BigDecimal fre = thirdListenBean.getFrequency();// 获取频度
				long freq = fre.longValue() * 60000;// 转换成分钟
				Timestamp tm = thirdListenBean.getBak1();// 获取最后一次更新的时间
				Date date = new Date();
				long nowDate = date.getTime();
				long dValue = nowDate - tm.getTime();
				String operation = "";// 日志描述
				Integer type = 2;// 日志类型 1：前台日志 2：后台日志 3： 更新日志
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String nowTime = sf.format(date);
				// System.out.println("nowDate:"+nowDate+" tm:"+tm.getTime()+" dValue:"+dValue);
				synchronized (this) {
					if (dValue > freq) {// 如果时间差大约频度时间则更新状态为0 未及时更新。
						thirdListenBean.setStatus("0");
						thirdlistenRespository.saveAndFlush(thirdListenBean);
						operation = nowTime + " 系统检测到 " + thirdListenBean.getBak2() + ":"
								+ thirdListenBean.getMethodname() + " 未准时推送信息";
						// interfaceService.saveLogInterface(operation, type);
					}
				}

			}
			try {
				Thread.sleep(Integer.parseInt(interfaceTime));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// public InterfaceLogService getInterfaceService() {
	// return interfaceService;
	// }

	// public void setInterfaceService(InterfaceLogService interfaceService) {
	// this.interfaceService = interfaceService;
	// }

	public Thirdlisten getThirdListenBean() {
		return thirdListenBean;
	}

	public void setThirdListenBean(Thirdlisten thirdListenBean) {
		this.thirdListenBean = thirdListenBean;
	}

	public ThirdlistenRespository getThirdlistenRespository() {
		return thirdlistenRespository;
	}

	public void setThirdlistenRespository(ThirdlistenRespository thirdlistenRespository) {
		this.thirdlistenRespository = thirdlistenRespository;
	}

	public String getInterfaceTime() {
		return interfaceTime;
	}

	public void setInterfaceTime(String interfaceTime) {
		this.interfaceTime = interfaceTime;
	}

}
