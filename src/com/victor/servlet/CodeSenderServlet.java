package com.victor.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.victor.utils.SmsUtil;
import com.victor.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class VerifiCodeServlet
 */
public class CodeSenderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CodeSenderServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 接收参数
		String phoneNo = request.getParameter("phone_no");

		if (phoneNo == null) {
			return;
		}
		
		Jedis jedis = new Jedis("192.168.67.143", 6379);
		
		// 验证使用次数 
		//1 存在 且少于次数? --+1 
		//2 存在 大于次数 返回limit 
		//3 不存? 正常发送?? 并且+1
		String countKey = VerifyCodeConfig.PHONE_PREFIX + phoneNo + VerifyCodeConfig.COUNT_SUFFIX;

		String countKeyStr = jedis.get(countKey);

		if (countKeyStr != null) {
			int count = Integer.parseInt(countKeyStr);
			if (count < 3) {
				jedis.incr(countKey);
			} else {
				response.getWriter().print("limit");
				jedis.close();
				return;
			}
		} else {
			jedis.setex(countKey, VerifyCodeConfig.SECONDS_PER_DAY, "1");
		}

		// 1 �? 生成验证�?
		String verifyCode = genCode(6);

		// 2�? 把验证码和手机号 存储到redis

		String verifyKey = VerifyCodeConfig.PHONE_PREFIX + phoneNo + VerifyCodeConfig.PHONE_SUFFIX;

		jedis.setex(verifyKey, VerifyCodeConfig.CODE_TIMEOUT, verifyCode);

		jedis.close();

		// 3、发送验证码给客户的手机?
		SmsUtil.sendSms(phoneNo, verifyCode);

		// 返回
		response.getWriter().print(true);

	}

	private String genCode(int len) {
		String code = "";
		for (int i = 0; i < len; i++) {
			int rand = new Random().nextInt(10);
			code += rand;
		}

		return code;
	}

}
