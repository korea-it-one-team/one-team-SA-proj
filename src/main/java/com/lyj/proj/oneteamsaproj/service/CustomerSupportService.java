package com.lyj.proj.oneteamsaproj.service;

import com.lyj.proj.oneteamsaproj.repository.CustomerSupportRepository;
import com.lyj.proj.oneteamsaproj.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerSupportService {

    @Autowired
    private CustomerSupportRepository customerSupportRepository;

    public List<Faq> getFaqs() {
        return customerSupportRepository.getFaqs();
    }

    public List<faq_Categorys> getcategorys() {
        return customerSupportRepository.getFaqs_Category();
    }

    public Consultation addConsultation(String title, String content, int member_id) {
        customerSupportRepository.addConsultation(title, content, member_id);
        return customerSupportRepository.lastConsultation();
    }

    public List<Consultation> getHistory(int member_id, String member_status) {

        if(member_status.equals("관리자")){
            return customerSupportRepository.allFind();
        }
        return customerSupportRepository.getHistory(member_id);
    }

}
