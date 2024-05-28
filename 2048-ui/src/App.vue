<template>
  <div id="app">
    <transition name="fade">
      <router-view/>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App',
  mounted() {
    // 监听键盘事件
    document.addEventListener('keydown', this.preventKeyboardNavigation);
  },
  beforeDestroy() {
    // 销毁时,移除监听器
    document.removeEventListener('keydown', this.preventKeyboardNavigation);
  },
  methods:{
    /**
     *  fix: -- 场景: 浏览器缩小化时, 使用方向盘会同时触发浏览器行为
     *       -- 2024-05-21
     *  禁止方向键控制浏览器滚动
     * @param event 事件
     */
    preventKeyboardNavigation(event) {
      // 判断是否为方向键
      if (event.keyCode >= 37 && event.keyCode <= 40) {
        event.preventDefault();
      }
    }
  }
}
</script>

<style>
@media only screen and (min-width: 466px){
  html{
    font-size: 14px;
  }
}
@media only screen and (max-width: 466px){
  html{
    font-size: 10px;
  }
}

* {
  margin: 0;
  padding: 0;
}
html, 
body {
  width: 100%;
  height: 100%;
}
button, 
input {
  outline: none;
  border-color: transparent;
  box-shadow:none;
  border: none;
}
button {
  cursor: pointer;
}
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  width: 100%;
  height: 100%;
  background-image: url('assets/img/bg1.jpeg');
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
</style>
