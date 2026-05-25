document.getElementById('fotoInput').addEventListener('change', function(event) {
    const archivo = event.target.files[0];
    if (archivo) {
        const lector = new FileReader();
        lector.onload = function(e) {
            // Cambia el src de la imagen por la foto que se acaba de seleccionar
            document.getElementById('vistaPrevia').src = e.target.result;
        }
        lector.readAsDataURL(archivo);
    }
});