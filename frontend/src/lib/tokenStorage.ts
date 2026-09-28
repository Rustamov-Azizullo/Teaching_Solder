const TOKEN_KEY = 'askar.accessToken';

// Xotira zaxirasi: brauzer saqlashni bloklasa (masalan, maxfiy rejim) joriy sessiya ishlashda davom etadi.
let memoryToken: string | null = null;

export const tokenStorage = {
  get(): string | null {
    try {
      // Saqlash mavjud bo'lsa, u yagona haqiqat manbai (boshqa tabda chiqilsa, bu yerda ham chiqiladi).
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return memoryToken;
    }
  },
  set(token: string): void {
    memoryToken = token;
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch {
      // Saqlash imkoni yo'q — token faqat xotirada qoladi.
    }
  },
  clear(): void {
    memoryToken = null;
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {
      // e'tiborsiz: o'chirilishi kerak bo'lgan narsa yo'q
    }
  },
};
