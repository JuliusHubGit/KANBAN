const API_URL = '/api/tasks';
const boardColumns = document.querySelectorAll('.column-body');
const form = document.getElementById('task-form');
const archiveSection = document.getElementById('archive');
const archiveList = document.getElementById('archive-list');
const showArchiveButton = document.getElementById('show-archive');
const template = document.getElementById('task-template');
let tasks = [];
let editingTaskId = null;

async function loadTasks() {
    const response = await fetch(API_URL);
    tasks = await response.json();
    renderBoard();
}

async function loadArchive() {
    const response = await fetch(`${API_URL}/archived`);
    const archived = await response.json();
    archiveList.innerHTML = '';
    archived.forEach(item => {
        const li = document.createElement('li');
        li.textContent = `${item.title} – erledigt am ${formatDate(item.archivedAt)}`;
        archiveList.appendChild(li);
    });
}

function renderBoard() {
    boardColumns.forEach(column => column.innerHTML = '');
    tasks.forEach(task => {
        const column = document.querySelector(`.column-body[data-status="${task.status}"]`);
        if (!column) {
            return;
        }
        const node = template.content.firstElementChild.cloneNode(true);
        node.dataset.id = task.id;
        node.querySelector('.task-title').textContent = task.title;
        node.querySelector('.task-description').textContent = task.description || 'Keine Beschreibung';
        node.querySelector('.responsible').textContent = task.assigneeName ? `Verantwortlich: ${task.assigneeName}` : 'Keine verantwortliche Person';
        node.querySelector('.due').textContent = task.dueDate ? `Fälligkeit: ${formatDate(task.dueDate)}` : 'Keine Fälligkeit';

        node.addEventListener('dragstart', onDragStart);
        node.addEventListener('dragend', onDragEnd);
        node.querySelector('.delete').addEventListener('click', () => deleteTask(task.id));
        node.querySelector('.edit').addEventListener('click', () => startEdit(task));

        column.appendChild(node);
    });
}

function onDragStart(event) {
    event.dataTransfer.setData('text/plain', event.target.dataset.id);
    event.target.classList.add('dragging');
}

function onDragEnd(event) {
    event.target.classList.remove('dragging');
}

boardColumns.forEach(column => {
    column.addEventListener('dragover', event => {
        event.preventDefault();
        column.classList.add('drag-over');
    });
    column.addEventListener('dragleave', () => column.classList.remove('drag-over'));
    column.addEventListener('drop', async event => {
        event.preventDefault();
        column.classList.remove('drag-over');
        const id = event.dataTransfer.getData('text/plain');
        const status = column.dataset.status;
        await moveTask(id, status);
    });
});

async function moveTask(id, status) {
    const response = await fetch(`${API_URL}/${id}/move`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
    });
    if (response.ok) {
        await loadTasks();
    } else {
        console.error('Fehler beim Verschieben');
    }
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    const payload = collectFormData();
    if (!payload) {
        return;
    }

    const method = editingTaskId ? 'PUT' : 'POST';
    const url = editingTaskId ? `${API_URL}/${editingTaskId}` : API_URL;

    const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (response.ok) {
        resetForm();
        await loadTasks();
    } else {
        const errorText = await response.text();
        alert(`Fehler: ${errorText}`);
    }
});

function collectFormData() {
    const title = form.title.value.trim();
    if (!title) {
        alert('Bitte gib einen Titel ein.');
        return null;
    }
    const description = form.description.value.trim();
    const assigneeName = form.assigneeName.value.trim();
    const assigneeEmail = form.assigneeEmail.value.trim();
    const dueDate = form.dueDate.value;
    const status = form.status.value;

    return {
        title,
        description: description || null,
        assigneeName: assigneeName || null,
        assigneeEmail: assigneeEmail || null,
        dueDate: dueDate || null,
        status
    };
}

function resetForm() {
    form.reset();
    form.status.value = 'TODO';
    editingTaskId = null;
    form.querySelector('button[type="submit"]').textContent = 'Aufgabe anlegen';
}

async function deleteTask(id) {
    if (!confirm('Diese Aufgabe wirklich löschen?')) {
        return;
    }
    const response = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
    if (response.ok) {
        await loadTasks();
    }
}

function startEdit(task) {
    editingTaskId = task.id;
    form.title.value = task.title;
    form.description.value = task.description || '';
    form.assigneeName.value = task.assigneeName || '';
    form.assigneeEmail.value = task.assigneeEmail || '';
    form.status.value = task.status;
    form.dueDate.value = task.dueDate || '';
    form.querySelector('button[type="submit"]').textContent = 'Aufgabe speichern';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

showArchiveButton.addEventListener('click', async () => {
    const hidden = archiveSection.hasAttribute('hidden');
    if (hidden) {
        await loadArchive();
        archiveSection.removeAttribute('hidden');
        showArchiveButton.textContent = 'Archiv verstecken';
    } else {
        archiveSection.setAttribute('hidden', 'hidden');
        showArchiveButton.textContent = 'Archiv anzeigen';
    }
});

function formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleDateString('de-DE');
}

loadTasks();
