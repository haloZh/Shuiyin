# -*- coding:utf-8 -*-
# datetime:2020/12/7 12:49
# @File:douyin.py
# author: 小q

import datetime
import os
import requests
import re
import sys


headers={
    'Accept':'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9',
    'User-Agent':'Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36'
}

#  Matcher matcher = Patterns.WEB_URL.matcher(data);
#     if (matcher.find()){
#         System.out.println(matcher.group());
#     }
def main(msg):

    if "douyin" in msg and "https://" in msg:
        try:
            # 抖音原始url
            url =  re.search('https://(.*?)/(.*?)/', msg).group(0)
            # print(url)
            r = requests.get(url, headers=headers)
            # 抖音重定向后的url
            url_1 = r.url
            com = re.compile('\d{19}')
            s = com.findall(url_1)
            item_id = s[0]
            get_ture_url = 'https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=' + item_id + '&dytk='
            r1 = requests.get(get_ture_url, headers=headers)
            r1.encoding = 'utf-8'
            json = r1.json()
            base_url = json['item_list'][0]['video']['play_addr']['url_list'][0]
            tt = base_url.replace('playwm', 'play')
            new = requests.get(tt, headers=headers)
            last = new.url
            return last
        except:
            return "视频链接错误，解析不成功，请更换链接！"
    else:
        return "目前只支持抖音链接，请重新输入！"




if __name__ == '__main__':
    url = sys.argv[1]
    new_url = main(url)
    print(new_url)

