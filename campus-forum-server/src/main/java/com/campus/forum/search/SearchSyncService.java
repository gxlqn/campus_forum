package com.campus.forum.search;

public interface SearchSyncService {

    void syncPost(Long id);

    void deletePost(Long id);

    void syncProduct(Long id);

    void deleteProduct(Long id);

    void syncActivity(Long id);

    void deleteActivity(Long id);

    void syncHelp(Long id);

    void deleteHelp(Long id);
}
