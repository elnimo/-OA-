package cn.com.taiji.pad2.thirdlisten.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * 第三方服务访问本服务接口监听实体
 * @author 林傲
 * @date 2017-08-16
 * 
 */

@Entity
@NamedQuery(name="Thirdlisten.findAll", query="SELECT t FROM Thirdlisten t")
public class Thirdlisten implements Serializable {
	private static final long serialVersionUID = 1L;
	//最后一次更新数据时间
	private Timestamp bak1;
	//厂商名
	private String bak2;
	//备用字段
	private String bak3;
	//备用字段
	private String bak4;
	//备用字段
	private String bak5;
	//频度 按分钟计算
	private BigDecimal frequency;
	//方法名
	private String methodname;
	//更新状态 0为未更新 1为已更新
	private String status;
	@Id
	private String tid;

	public Thirdlisten() {
	}

	public Timestamp getBak1() {
		return this.bak1;
	}

	public void setBak1(Timestamp bak1) {
		this.bak1 = bak1;
	}

	public String getBak2() {
		return this.bak2;
	}

	public void setBak2(String bak2) {
		this.bak2 = bak2;
	}

	public String getBak3() {
		return this.bak3;
	}

	public void setBak3(String bak3) {
		this.bak3 = bak3;
	}

	public String getBak4() {
		return this.bak4;
	}

	public void setBak4(String bak4) {
		this.bak4 = bak4;
	}

	public String getBak5() {
		return this.bak5;
	}

	public void setBak5(String bak5) {
		this.bak5 = bak5;
	}

	public BigDecimal getFrequency() {
		return this.frequency;
	}

	public void setFrequency(BigDecimal frequency) {
		this.frequency = frequency;
	}

	public String getMethodname() {
		return this.methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTid() {
		return this.tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

}