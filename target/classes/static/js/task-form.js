// Global Task Form Logic
let editingTaskId = null;
let cachedCategories = null;
let taskFormModalInstance = null;

function getTaskFormModal() {
    if (!taskFormModalInstance) {
        const modalEl = document.getElementById('taskFormModal');
        if (modalEl) {
            taskFormModalInstance = new bootstrap.Modal(modalEl);
        }
    }
    return taskFormModalInstance;
}

async function loadCategories() {
    if (cachedCategories) return cachedCategories;
    try {
        const response = await fetch('/api/categories');
        cachedCategories = await response.json();
        
        const select = document.getElementById('formTaskCategory');
        if (!select) return [];
        
        select.innerHTML = '<option value="" class="text-dark">No Category</option>';
        cachedCategories.forEach(cat => {
            const opt = document.createElement('option');
            opt.value = cat.id;
            opt.textContent = cat.name;
            opt.className = 'text-dark';
            select.appendChild(opt);
        });
        return cachedCategories;
    } catch(e) { 
        console.error("Error loading categories:", e); 
        return []; 
    }
}

async function openCreateModal() {
    editingTaskId = null;
    const form = document.getElementById('taskForm');
    if (form) form.reset();
    
    const titleEl = document.getElementById('formModalTitle');
    if (titleEl) titleEl.innerText = "Create New Task";
    
    const iconCreate = document.getElementById('modalIconCreate');
    const iconEdit = document.getElementById('modalIconEdit');
    if (iconCreate) iconCreate.classList.remove('d-none');
    if (iconEdit) iconEdit.classList.add('d-none');
    
    await loadCategories();

    const dueEl = document.getElementById('formTaskDue');
    if (dueEl) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setMinutes(tomorrow.getMinutes() - tomorrow.getTimezoneOffset());
        dueEl.value = tomorrow.toISOString().slice(0, 16);
    }

    const modal = getTaskFormModal();
    if (modal) modal.show();
}

async function openEditModal(task) {
    if (!task) return;
    editingTaskId = task.id;

    const titleEl = document.getElementById('formModalTitle');
    if (titleEl) titleEl.innerText = "Update Task Details";
    
    const iconCreate = document.getElementById('modalIconCreate');
    const iconEdit = document.getElementById('modalIconEdit');
    if (iconEdit) iconEdit.classList.remove('d-none');
    if (iconCreate) iconCreate.classList.add('d-none');

    await loadCategories();

    const taskIdEl = document.getElementById('formTaskId');
    const taskTitleEl = document.getElementById('formTaskTitle');
    const taskDescEl = document.getElementById('formTaskDesc');
    const taskCatEl = document.getElementById('formTaskCategory');
    const taskDueEl = document.getElementById('formTaskDue');
    const taskPriorityEl = document.getElementById('formTaskPriority');

    if (taskIdEl) taskIdEl.value = task.id;
    if (taskTitleEl) taskTitleEl.value = task.title;
    if (taskDescEl) taskDescEl.value = task.description || '';
    if (taskCatEl) taskCatEl.value = task.categoryId || '';
    
    if (taskDueEl) {
        if (task.dueDate) {
            taskDueEl.value = task.dueDate.slice(0, 16);
        } else {
            taskDueEl.value = '';
        }
    }
    
    if (taskPriorityEl) taskPriorityEl.value = (task.priority || 'MEDIUM').toUpperCase();

    const modal = getTaskFormModal();
    if (modal) modal.show();
}

async function saveTask() {
    const titleEl = document.getElementById('formTaskTitle');
    const title = titleEl ? titleEl.value.trim() : '';
    if(!title) {
        if (window.showAppToast) window.showAppToast('Title is required!', 'danger');
        else alert('Title is required!');
        return;
    }

    const desc = document.getElementById('formTaskDesc').value.trim();
    const due = document.getElementById('formTaskDue').value;
    const prio = document.getElementById('formTaskPriority').value;
    const catId = document.getElementById('formTaskCategory').value;

    const payload = { 
        title, 
        description: desc, 
        dueDate: due ? due + ":00" : null,
        priority: prio,
        categoryId: catId ? parseInt(catId) : null
    };

    try {
        const method = editingTaskId ? 'PUT' : 'POST';
        const url = editingTaskId ? `/api/tasks/${editingTaskId}` : `/api/tasks`;
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const modal = getTaskFormModal();
            if (modal) modal.hide();
            
            // Check for page-specific refresh function
            if (typeof window.refreshTaskList === 'function') {
                await window.refreshTaskList();
            }
            
            if (window.showAppToast) {
                window.showAppToast(editingTaskId ? "Task updated!" : "Task created!");
            }
        } else {
            throw new Error("Failed to save");
        }
    } catch(e) {
        console.error(e);
        if (window.showAppToast) window.showAppToast("Failed to save task!", "danger");
    }
}
