from playwright.sync_api import sync_playwright
import time
import re
import os


def exportar_subsecciones_a_pdf():
    carpeta_salida = "PDFs_STUDER"
    os.makedirs(carpeta_salida, exist_ok=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        # Forzamos el esquema de color claro desde el navegador
        context = browser.new_context(color_scheme="light")
        page = context.new_page()

        base_url = "https://deepwiki.com"
        main_url = f"{base_url}/FacuRochaS/STUDER/"

        print(f"Accediendo a {main_url}...")
        page.goto(main_url, wait_until="networkidle")

        print("Mapeando el menú de navegación...")
        elementos_link = page.eval_on_selector_all(
            "a[href^='/FacuRochaS/STUDER/']",
            "elements => elements.map(e => ({ titulo: e.innerText, url: e.href }))"
        )

        vistos = set()
        secciones = []
        for link in elementos_link:
            url = link['url']
            titulo = link['titulo'].strip()
            if url not in vistos and url != main_url and titulo:
                vistos.add(url)
                secciones.append(link)

        print(f"Se encontraron {len(secciones)} subsecciones. Iniciando la descarga...\n")

        for i, seccion in enumerate(secciones):
            url = seccion['url']
            titulo = seccion['titulo']

            nombre_archivo = re.sub(r'[\\/*?:"<>|]', "", titulo)
            nombre_archivo = f"{i + 1:02d} - {nombre_archivo}.pdf"
            ruta_completa = os.path.join(carpeta_salida, nombre_archivo)

            print(f"Procesando ({i + 1}/{len(secciones)}): {titulo}")
            page.goto(url, wait_until="networkidle")

            # --- LA ESTRATEGIA DE EXTRACCIÓN PURA ---
            page.evaluate("""
                // 1. Abrir desplegables primero para que rendericen su contenido
                document.querySelectorAll('details').forEach(d => d.setAttribute('open', 'true'));
                document.querySelectorAll('button[aria-expanded="false"]').forEach(btn => btn.click());

                // 2. Encontrar el contenedor exacto que me pasaste
                const contenidoPrincipal = document.querySelector('.prose');

                if (contenidoPrincipal) {
                    // 3. Arrancar todo el resto de la página y dejar SOLO tu documentación
                    document.body.innerHTML = '';
                    document.body.appendChild(contenidoPrincipal);

                    // 4. Limpiar clases de "Modo Oscuro" para que el texto sea oscuro nativamente
                    contenidoPrincipal.classList.remove('prose-invert', 'dark:prose-invert');

                    // Limpiar el color gris/blanco forzado en el código que encontraste
                    document.querySelectorAll('*').forEach(el => {
                        if (el.classList.contains('text-neutral-300')) {
                            el.classList.remove('text-neutral-300');
                            el.style.color = '#1a1a1a'; // Gris muy oscuro/negro
                        }
                        if (el.classList.contains('text-white')) {
                            el.classList.remove('text-white');
                            el.style.color = '#1a1a1a';
                        }
                    });
                }

                // 5. Forzar fondos a blanco puro
                document.documentElement.style.backgroundColor = '#ffffff';
                document.body.style.backgroundColor = '#ffffff';
                document.body.style.backgroundImage = 'none';

                // 6. Eliminar cualquier margen molesto
                document.body.style.padding = '0px';
                document.body.style.margin = '0px';
            """)

            # Pausa para que el DOM se reorganice
            time.sleep(2)

            page.pdf(
                path=ruta_completa,
                format="A4",
                margin={"top": "20mm", "bottom": "20mm", "left": "20mm", "right": "20mm"},
                print_background=True
            )
            print(f"-> Guardado con éxito: {ruta_completa}")

        browser.close()
        print(f"\n¡Proceso finalizado! Todos los PDFs se generaron en la carpeta '{carpeta_salida}'.")


if __name__ == "__main__":
    exportar_subsecciones_a_pdf()