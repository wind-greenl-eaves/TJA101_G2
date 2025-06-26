package com.eatfast.news.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepo;

    public List<NewsEntity> getAllNews() {
        return newsRepo.findAll();
    }

    public Optional<NewsEntity> getNewsById(Long id) {
        return newsRepo.findById(id);
    }

    public NewsEntity saveOrUpdateNews(NewsEntity news) {
        return newsRepo.save(news);
    }

    public void deleteNewsById(Long id) {
        newsRepo.deleteById(id);
    }

    //模糊查詢：標題中含有關鍵字
    public List<NewsEntity> searchByTitle(String keyword) {
        return newsRepo.findByTitleContaining(keyword);
    }

    //根據狀態查詢（例：上架中 status = 1）
    public List<NewsEntity> findByStatus(Integer status) {
        return newsRepo.findByStatus(status);
    }

}

