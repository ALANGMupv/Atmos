
// Datos simulados (Frontend only por ahora)
const MOCK_DATA = [
    {
        id: 101,
        user: "Juan Pérez",
        title: "Fallo en sensor norte",
        date: "2025-09-08 09:41:00",
        status: "pending",
        desc: "Hola, he notado que el sensor del sector norte ha dejado de enviar datos desde ayer por la noche.",
        response: ""
    },
    {
        id: 102,
        user: "Maria Lopez",
        title: "Solicitud nueva placa",
        date: "2025-09-08 10:15:00",
        status: "pending",
        desc: "Quisiera solicitar una placa adicional para el invernadero trasero.",
        response: ""
    },
    {
        id: 99,
        user: "Carlos Garcia",
        title: "Duda sobre factura",
        date: "2025-09-05 14:00:00",
        status: "resolved",
        desc: "No entiendo el cobro de este mes.",
        response: "Hola Carlos, el cobro incluye la cuota anual. Saludos."
    }
];

// Para probar el "Empty State", cambia esta variable a true en la consola
let forceEmpty = false;

document.addEventListener('DOMContentLoaded', () => {
    renderList(MOCK_DATA);

    // Filter listeners
    document.querySelectorAll('.btn-filter').forEach(btn => {
        btn.addEventListener('click', () => {
            // UI Toggle
            document.querySelectorAll('.btn-filter').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Logic
            const filter = btn.dataset.filter; // 'all', 'pending', 'resolved'
            applyFilter(filter);
        });
    });
});

function applyFilter(filter) {
    if (forceEmpty) {
        renderList([]);
        return;
    }

    if (filter === 'all') {
        renderList(MOCK_DATA);
    } else {
        const filtered = MOCK_DATA.filter(item => item.status === filter);
        renderList(filtered);
    }
}

function renderList(data) {
    const container = document.getElementById('adminIncidenceList');
    container.innerHTML = '';

    if (data.length === 0) {
        // EMPTY STATE
        container.innerHTML = `
            <div class="empty-inbox">
                <div class="empty-icon-wrapper">
                    <i class="fa-regular fa-folder-open"></i>
                </div>
                <h3>Bandeja vacía</h3>
                <p>No hay incidencias en esta vista.</p>
            </div>
        `;
        return;
    }

    data.forEach(inc => {
        const item = document.createElement('div');
        item.className = 'incidence-item';
        item.dataset.id = inc.id;

        // Status logic
        let dotClass = 'yellow';
        let statusTitle = 'Pendiente';
        if (inc.status === 'resolved') { dotClass = 'green'; statusTitle = 'Resuelta'; }
        if (inc.status === 'rejected') { dotClass = 'red'; statusTitle = 'Rechazada'; }

        item.innerHTML = `
            <div class="incidence-header">
                <div class="incidence-user-info"><i class="fa-solid fa-user"></i> ${inc.user}</div>
                <h4 class="incidence-title-item">${inc.title}</h4>
                <div class="incidence-meta">
                    <span class="incidence-time">${formatearFecha(inc.date)}</span>
                    <span class="status-dot ${dotClass}" title="${statusTitle}"></span>
                </div>
            </div>
            <p class="incidence-preview">${inc.desc}</p>
        `;

        item.addEventListener('click', () => selectIncidencia(item, inc));
        container.appendChild(item);
    });
}

function selectIncidencia(element, data) {
    // UI active class
    document.querySelectorAll('.incidence-item').forEach(i => i.classList.remove('active'));
    element.classList.add('active');

    // Render Right Panel
    document.getElementById('detailUser').innerHTML = `<i class="fa-solid fa-user"></i> ${data.user}`;
    document.getElementById('detailTitle').textContent = data.title;
    document.getElementById('detailDesc').textContent = data.desc;

    // Form logic
    const textarea = document.getElementById('responseInput');
    textarea.value = data.response || "";

    // Reset buttons
    document.querySelectorAll('.btn-action').forEach(b => b.classList.remove('active'));

    // Set active status button
    if (data.status === 'resolved') document.querySelector('.btn-resolve').classList.add('active');
    if (data.status === 'rejected') document.querySelector('.btn-reject').classList.add('active');
}

function formatearFecha(dateString) {
    // Simple format
    const d = new Date(dateString);
    return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')} - ${d.getDate()}/${d.getMonth() + 1}/${d.getFullYear()}`;
}
