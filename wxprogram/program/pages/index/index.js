// index.js
Page({
  data:{
    msg: "hello world!",
    nickName: "",
    url: ""
  },
  // 获取头像和昵称
  getUserInfo() {
    wx.getUserProfile({
      desc: '获取用户信息',
      success: (res) => {
        console.log(res.userInfo)
        this.setData({
          nickName: res.userInfo.nickName,
          url: res.userInfo.avatarUrl
        })
      }
    })
  },
  // 微信登录，获取用户的授权码
  wxLogin() {
    wx.login({
      success: (res) => {
        console.log(res.code)
        this.setData({
          code: res.code
        })
      },
    })
  },

  // 发送请求
  sendRequest() {
    wx.request({
      url: 'http://localhost:8080/user/shop/status',
      method: 'GET',
      success: (res) => {
        console.log(res.data)
      }
    })
  }

})
