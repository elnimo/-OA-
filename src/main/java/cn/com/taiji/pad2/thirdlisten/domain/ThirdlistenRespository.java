package cn.com.taiji.pad2.thirdlisten.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdlistenRespository extends JpaRepository<Thirdlisten, String>,JpaSpecificationExecutor<Thirdlisten>{

}
