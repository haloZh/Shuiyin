# -*- coding:utf-8 -*-
# datetime:2020/12/7 12:49
# @File:douyin.py
# author: 小q

import datetime
import os
import requests
import re


headers={
    'Accept':'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9',
    'User-Agent':'Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36'
}

def main(msg):
    if "抖音" in msg and "https://" in msg:
        try:
            # 抖音原始url
            url = "https://" + re.search('https://(.*?) 复制', msg).group(1)
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
        return "不是抖音链接，请重新输入！"




if __name__ == '__main__':
    new_url = main("抖音 https://")
    # new_url = main(" 父母留给孩子最重要的资产是爱，是一幅幅画面，所以这就是言传身教的积极影响。 %家庭教育  %育儿  %亲子  https://v.douyin.com/JgU3RdQ/ 复制此链接，打开抖音搜索，直接观看视频")
    print(new_url)

