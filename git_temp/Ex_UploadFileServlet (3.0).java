package ido.school.listener;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/UploadFileServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, 	// 2MB   ��ƾڶq�j��ӭȮɡA���e�}�l�g�J�w�СC�w�]0
		maxFileSize = 1024 * 1024 * 150, 	// 150MB ���ɤ��̤j�e�q�W���A�w�]-1 (������)
		maxRequestSize = 1024 * 1024 * 500) 	// 500MB �W�ǥ������e�q�W���A�w�]-1 (������)

// ���{���O servlet 3.0 �� (2.x�g�k���P) �d����:�M�ץk��>properties>project facets>dynamic web module
public class UploadFileServlet extends HttpServlet {

	private static final String SAVE_DIR = "uploadFiles";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String message = "";
		try {
			// gets absolute path of the web application
			String appPath = request.getServletContext().getRealPath("");
			
			// constructs path of the directory to save uploaded file
			String savePath = appPath + File.separator + SAVE_DIR;
			
			// creates the save directory if it does not exists
			File fileSaveDir = new File(savePath);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdir();
			}

			String fileName = "";
			for (Part part : request.getParts()) {
				fileName = extractFileName(part);
				part.write(savePath + File.separator + fileName);
			}
	        message = "success,"+fileName;
			
		} catch (Exception e) {
			message = "fail,"+ e.getMessage();
		} finally {
	        try {
	        	//request.setAttribute("message", message);
	        	response.setContentType("text/html;charset=UTF-8");
				response.getWriter().write(message);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	/**
	 * Extracts file name from HTTP header content-disposition
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}
}