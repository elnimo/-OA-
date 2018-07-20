package cn.com.taiji.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.bjca.sso.bean.UserTicket;
import com.bjca.sso.processor.TicketManager;

import cn.com.taiji.pad2.aop.util.AopSaveUtil;
import cn.com.taiji.security.TaijiAuthenticationProvider;
import cn.com.taiji.sys.domain.Menu;
import cn.com.taiji.sys.domain.Role;
import cn.com.taiji.sys.domain.User;
import cn.com.taiji.sys.dto.KendoTreeViewDto;
import cn.com.taiji.sys.service.MenuService;
import cn.com.taiji.sys.service.UserService;

/**
 * 
 * @author chixue
 *
 */
@Controller
public class HomeController {

	@Inject
	private MenuService menuService;

	@Inject
	private UserService userService;

	@Inject
	private AopSaveUtil aopSaveUtil;

	@Inject
	private TaijiAuthenticationProvider authenticationManager;

	/**
	 * 跳转至主页
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/")
	public String home(Model model) {
		return "index";// semantic
	}

	@RequestMapping("/some")
	public String some(Model model) {
		return "some";// semantic
	}

	@RequestMapping("/authorization-menu")
	public String authorizationMenu(Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		/*
		 * if(user!=null){ List<Role> userRole = user.getRoles(); List<Menu> menus = new
		 * ArrayList<Menu>(); if(userRole.size() > 0 ) menus =
		 * menuService.findRootByAuthorization(userRole); KendoTreeViewDto treeViewDto =
		 * new KendoTreeViewDto(); List<KendoTreeViewDto> list =
		 * treeViewDto.menuAuthorizationTree(menus,userRole);
		 * model.addAttribute("menuTree", list); }
		 */
		if (user != null) {
			if ("admin".equals(user.getLoginName())) {
				List<Menu> menus = menuService.findRootTree();
				// 使用kendoui的kendoTreeView实现
				KendoTreeViewDto treeViewDto = new KendoTreeViewDto();
				List<KendoTreeViewDto> list = treeViewDto.menuTree(menus);
				model.addAttribute("menuTree", list);
			} else {
				List<Role> userRole = user.getRoles();
				List<Menu> menus = new ArrayList<Menu>();
				if (userRole.size() > 0)
					menus = menuService.findRootByAuthorization(userRole);
				KendoTreeViewDto treeViewDto = new KendoTreeViewDto();
				List<KendoTreeViewDto> list = treeViewDto.menuAuthorizationTree(menus, userRole);
				model.addAttribute("menuTree", list);
			}
		}

		return "index";// semantic
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	// for 403 access denied page
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public ModelAndView accesssDenied(Principal user) {
		ModelAndView model = new ModelAndView();
		if (user != null) {
			model.addObject("msg", "Hi " + user.getName() + ", you do not have permission to access this page!");
		} else {
			model.addObject("msg", "You do not have permission to access this page!");
		}
		model.setViewName("403");
		return model;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/image", method = RequestMethod.GET)
	public String image(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		BufferedImage img = new BufferedImage(68, 22, BufferedImage.TYPE_INT_RGB);
		// 得到该图片的绘图对象
		Graphics g = img.getGraphics();
		Random r = new Random();
		Color c = new Color(200, 150, 255);
		g.setColor(c);
		// 填充整个图片的颜色
		g.fillRect(0, 0, 68, 22);
		// 向图片中输出数字和字母
		StringBuffer sb = new StringBuffer();
		char[] ch = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		int index, len = ch.length;
		for (int i = 0; i < 4; i++) {
			index = r.nextInt(len);
			g.setColor(new Color(r.nextInt(88), r.nextInt(188), r.nextInt(255)));
			g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 22));
			// 输出的 字体和大小
			g.drawString("" + ch[index], (i * 15) + 3, 18);
			// 写什么数字，在图片 的什么位置画
			sb.append(ch[index]);
		}
		request.getSession().setAttribute("j_captcha", sb.toString());
		try {
			ImageIO.write(img, "JPEG", response.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping("/caslogin")
	public void cas(HttpServletRequest request, HttpServletResponse response) {

		// 服务器证书（认证服务器上的公钥，便于验证签名使用）
		String BJCA_SERVER_CERT = request.getParameter("BJCA_SERVER_CERT");
		// 票据 必须转换为GBK，便于显示生僻字如“旸”
		String BJCA_TICKET = null;

		try {
			BJCA_TICKET = new String(request.getParameter("BJCA_TICKET").getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 票据类型: 0=加密+签名; 1=未加密+签名; 2=未加密+未签名; 2=加密;
		String BJCA_TICKET_TYPE = request.getParameter("BJCA_TICKET_TYPE");

		// 目标url
		// String BJCA_TARGET_URL = request.getParameter("BJCA_TARGET_URL");

		String BJCA_TARGET_URL = "index";
		// 打印信息：
		// System.out.println("BJCA_SERVER_CERT==========" + BJCA_SERVER_CERT);
		// System.out.println("BJCA_TICKET==========" + BJCA_TICKET );
		// System.out.println("BJCA_TICKET_TYPE==========" + BJCA_TICKET_TYPE );
		// System.out.println("BJCA_TARGET_URL==========" + BJCA_TARGET_URL);

		// String ExtParam = request.getParameter("ExtParam");
		// System.out.println("ExtParam==========" + ExtParam );
		// System.out.println("BJCA_SERVER_CERT="+BJCA_SERVER_CERT);

		TicketManager ticketmag = new TicketManager();

		// 验证签名及解密
		UserTicket userticket = ticketmag.getTicket(BJCA_TICKET, BJCA_TICKET_TYPE, BJCA_SERVER_CERT);
		System.out.println("userticket==========" + userticket);
		// 处理票据信息
		if (userticket != null) {
			// 用户姓名
			String username = userticket.getUserName();// 这个是由“BJCA公司” 配置的选项，如果没有值需要告知“BJCA公司”。
			// String username = new String(userticket.getUserName().getBytes("GBK"),"GBK");
			// System.out.println("username111111111111======="+username);
			// 用户3 2位唯一标识码
			String userid = userticket.getUserUniqueID();
			// 用户所在部门的编码
			String departid = userticket.getUserDepartCode();
			// 用户所在部门的名称
			String departname = userticket.getUserDepartName();
			// departname = new
			// String(userticket.getUserDepartName().getBytes("ISO-8859-1"),"GB2312");
			// 用户所拥有的角色信息
			Hashtable roles = userticket.getUserRoles();
			String s_role = "";
			// if(roles != null && roles.size() > 0) {
			// int index = 1;
			// Enumeration e = roles.keys();
			// Enumeration e2 = roles.elements();
			// for(;e.hasMoreElements();){
			// String rolecode = (String)e.nextElement();
			// String rolename = (String)e2.nextElement();
			// if(rolename.indexOf("?") != -1) {
			// try {
			// rolename = new String(rolename.getBytes("ISO-8859-1"),"GB2312");
			// } catch (UnsupportedEncodingException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// // TODO Auto-generated catch block
			//
			// }
			// if(index == 1){
			// s_role = rolecode;
			// }else{
			// s_role = s_role + "," + rolecode;
			// }
			// index++;
			// System.out.println("s_role======="+s_role);
			// System.out.println("rolename======="+rolename);
			// }
			// }
			// 打印信息：
			// System.out.println("username======="+username);
			// System.out.println("userid======="+userid);
			// System.out.println("departid======="+departid);
			// 将用户的真实姓名、用户id、用户所属部门的id、角色信息写入SESSION中
			// request.getSession().setAttribute("username",username);
			// request.getSession().setAttribute("userid",userid);
			// request.getSession().setAttribute("departid",departid);
			// request.getSession().setAttribute("departname",departname);
			// request.getSession().setAttribute("roles",s_role);

			// a088ac766d9aaaf2824337c525c4f3b8
			User user = userService.findByLoginName(userid);
			if (user != null) {
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userid, "admin");
				try {
					token.setDetails(new WebAuthenticationDetails(request));
					Authentication authenticatedUser = authenticationManager.authenticate(token);

					SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
					request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
							SecurityContextHolder.getContext());
				} catch (AuthenticationException e) {
					// System.out.println("Authentication failed: " + e.getMessage());
					e.printStackTrace();
				}

				// 保存登录日志记录
				String operation = "用户：" + user.getUserName() + ",登录值班快报。";
				// --日志类型 1：前台日志 2：后台日志 3 更新日志
				aopSaveUtil.saveLogServer(user.getUserName(), operation, 2);

				HttpSession session = request.getSession(true);
				session.setAttribute("user", user);
				try {
					request.getRequestDispatcher(BJCA_TARGET_URL).forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// response.sendRedirect("./index.jsp");
			/**
			 * 对于如何进入自己的业务系统的一点解释：
			 * //request.getRequestDispatcher("uamslogin.do?action=SingleLogin")
			 * //.forward(request, response);//请各家自己写转向业务系统的代码。
			 * 可以通过获得的用户32位码（身份特征码），在数据库进行唯一匹配，进行业务处理。
			 */

		} else {
			// response.sendRedirect("error");//这是测试的错误页面。
			try {
				request.getRequestDispatcher("error").forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}