package cn.com.taiji.infutil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.taiji.pad2.aop.service.SaveLogService;
import cn.com.taiji.pad2.management.domain.InterfaceGl;
import cn.com.taiji.pad2.management.domain.InterfaceRepository;
//import cn.com.taiji.pad2.thirdlisten.log.InterfaceLogService;

/**
 * 接口状态监听服务
 * 
 * @author 林傲
 * @date 2017-08-14
 */
@Service
public class InterfaceListenService {
	
	@Autowired
	InterfaceRepository infR;

	// @Autowired
	// private InterfaceLogService interfaceService;
	
	/**
	 * 监听数据库中记录的接口地址是否正常访问
	 * 
	 * @return 不正常的接口地址InterfaceGl List
	 */
	public Map<String, List<InterfaceGl>> interfaceListen() {
		int state = 404;
		List<InterfaceGl> erroUrl = new ArrayList<InterfaceGl>();
		List<InterfaceGl> bingoUrl = new ArrayList<InterfaceGl>();
		Map<String, List<InterfaceGl>> infMap = new HashMap<String, List<InterfaceGl>>();
		List<InterfaceGl> infList = infR.findAll();// 获取所有接口实体
		String operation = "";// 日志描述
		Integer type = 2;// 日志类型 1：前台日志 2：后台日志 3： 更新日志
		Date date = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = sf.format(date);
		// 遍历所有实体将访问不通和正常接口分类存储
		for (InterfaceGl interfaceGl : infList) {
			String url = interfaceGl.getResurl();
			try {
				state = InterfaceUtil.testWsdlConnection(url);
				if (state == 404) {
					operation = nowTime + " 系统对 " + interfaceGl.getResname() + ":" + url + " 接口进行了监听,访问失败";
					// interfaceService.saveLogInterface(operation, type);
					erroUrl.add(interfaceGl);
				} else if (state == 200) {
					operation = nowTime + " 系统对 " + interfaceGl.getResname() + ":" + url + " 接口进行了监听,访问成功";
					// interfaceService.saveLogInterface(operation, type);
					bingoUrl.add(interfaceGl);
				}
			} catch (Exception e) {
				erroUrl.add(interfaceGl);
			}
		}
		infMap.put("erro", erroUrl);
		infMap.put("bingo", bingoUrl);
		return infMap;
	}

	/**
	 * 改变实体状态录入数据库中
	 * 
	 * @param inG
	 *            接口实体
	 * @param state
	 *            访问状态
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateInf(InterfaceGl inG, String state) {
		inG.setMonitorstate(state);// 1为正常访问 0为不正常
		infR.saveAndFlush(inG);
	}

}
