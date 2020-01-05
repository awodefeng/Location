package com.xxun.watch.stepcountservices.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by zhangjun5 on 2017/9/19.
 */

public class MyContentProviderMetaData {
    //URI的指定，此处的字符串必须和声明的authorities一致
    public static final String AUTHORITIES = "com.xxun.watch.stepCountProvider";
    //数据库名称
    public static final String DATABASE_NAME = "myContentProvider.db";
    //数据库的版本
    public static final int DATABASE_VERSION = 1;
    //表名
    public static final String USERS_TABLE_NAME = "user";

    public static final class UserTableMetaData implements BaseColumns {
        //表名
        public static final String TABLE_NAME = "steps";
        //访问该ContentProvider的URI
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/user");
        //该ContentProvider所返回的数据类型的定义
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.myprovider.user";
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.myprovider.user";
        //列名
        public static final String USER_NAME = "step_local";
        //默认的排序方法
        public static final String DEFAULT_SORT_ORDER = "_id desc";
    }

}
