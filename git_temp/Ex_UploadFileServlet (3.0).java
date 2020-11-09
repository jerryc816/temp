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
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, 	// 2MB   當數據量大於該值時，內容開始寫入硬碟。預設0
		maxFileSize = 1024 * 1024 * 150, 	// 150MB 單檔文件最大容量上限，預設-1 (不限制)
		maxRequestSize = 1024 * 1024 * 500) 	// 500MB 上傳全部文件容量上限，預設-1 (不限制)

// 此程式是 servlet 3.0 版 (2.x寫法不同) 查版本:專案右鍵>properties>project facets>dynamic web module
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