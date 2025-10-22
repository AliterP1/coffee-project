<template>
  <div class="register-page">
     <Header />
    <main class="main-content">
      <RegisterForm />
    </main>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import RegisterForm from '@/components/RegisterForm.vue'
</script>
<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}
</style>