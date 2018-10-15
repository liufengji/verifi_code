package com.victor.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.victor.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class CodeVerifyServlet
 */
public class CodeVerifyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CodeVerifyServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 1�����ղ���
		String phoneNo = request.getParameter("phone_no");

		String verifyCode = request.getParameter("verify_code");

		if (phoneNo == null || verifyCode == null) {
			return;
		}

		// 2�� ���� �ֻ��� ȡ�� redis���code

		Jedis jedis = new Jedis("192.168.67.143", 6379);

		String verifyKey = VerifyCodeConfig.PHONE_PREFIX + phoneNo + VerifyCodeConfig.PHONE_SUFFIX;

		String expectedCode = jedis.get(verifyKey);

		jedis.close();
		
		// 3 ��ҳ�洫������code �� ��̨���code �ȶ�
		if (verifyCode.equals(expectedCode)) {
			response.getWriter().print(true);
			return;
		}
		response.getWriter().print(false);

	}

}
