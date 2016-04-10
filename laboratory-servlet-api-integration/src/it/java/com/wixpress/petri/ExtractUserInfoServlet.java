package com.wixpress.petri;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_USER_INFO_STORAGE;

/**
 * @author Laurent_Gaertner
 * @since 11-Apr-2016
 */
public class ExtractUserInfoServlet extends BaseHttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserInfoStorage uiStorage = (UserInfoStorage) session.getAttribute(PETRI_USER_INFO_STORAGE);
        UserInfo userInfo = uiStorage.read();

        writeAsJsonResponse(response, userInfo);
     }
}
