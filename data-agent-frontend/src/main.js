/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { createApp } from 'vue';
import App from '@/App.vue';
import router from '@/router';
import axios from 'axios';
import { getAuth, toBasicAuthHeader } from '@/services/auth';

// Axios 拦截器：自动附加 Basic Auth 头
axios.interceptors.request.use((config) => {
  const auth = getAuth();
  if (auth) {
    config.headers.Authorization = toBasicAuthHeader(auth);
  }
  return config;
});

// 401 时清除登录态并跳转回登录页
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const { clearAuth } = require('@/services/auth');
      clearAuth();
      if (router.currentRoute.value.name !== 'Login') {
        router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } });
      }
    }
    return Promise.reject(error);
  },
);

// 引入全局样式
import '@/styles/global.css';
import 'element-plus/dist/index.css';
import ElementPlus from 'element-plus';

// 创建应用实例
const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
