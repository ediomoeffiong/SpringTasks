/**
 * Collaboration & Assignment System
 * Globally accessible logic for managing task collaborators
 */

let currentAssignmentTaskId = null;
let assignmentModal = null;

document.addEventListener('DOMContentLoaded', () => {
    const modalEl = document.getElementById('assignmentModal');
    if (modalEl) {
        assignmentModal = new bootstrap.Modal(modalEl);
    }
});

/**
 * Main entry point: Opens the collaboration modal for a specific task
 */
async function openAssignmentModal(taskId, taskTitle) {
    currentAssignmentTaskId = taskId;
    
    // Set UI state
    document.getElementById('assignTaskTitle').innerText = taskTitle || `Task #${taskId}`;
    document.getElementById('inviteUsernameInput').value = '';
    document.getElementById('inviteFeedback').innerText = '';
    
    // Show modal
    assignmentModal.show();
    
    // Fetch and render data
    await refreshCollaborationUI();
}

/**
 * Fetches all necessary data and re-renders the list sections
 */
async function refreshCollaborationUI() {
    if (!currentAssignmentTaskId) return;

    try {
        // Fetch collaborators and pending invites in parallel
        const [collabRes, pendingRes] = await Promise.all([
            fetch(`/api/tasks/${currentAssignmentTaskId}/collaborators`),
            fetch(`/api/tasks/${currentAssignmentTaskId}/pending-invitations`)
        ]);

        if (!collabRes.ok || !pendingRes.ok) throw new Error('Failed to fetch data');

        const collaborators = await collabRes.json();
        const pendingInvites = await pendingRes.json();

        renderCollaborators(collaborators);
        renderPendingInvites(pendingInvites);
    } catch (error) {
        console.error('Error refreshing collaboration UI:', error);
        showToast('error', 'Failed to load collaboration data');
    }
}

function renderCollaborators(collaborators) {
    const container = document.getElementById('collabList');
    document.getElementById('collabCount').innerText = collaborators.length;

    if (collaborators.length === 0) {
        container.innerHTML = `
            <div class="text-center py-3 text-muted opacity-50 small">
                <i class="bi bi-people mb-2 d-block fs-3"></i>
                No collaborators yet
            </div>
        `;
        return;
    }

    container.innerHTML = collaborators.map(u => `
        <div class="collab-item d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
                <div class="avatar-circle">${u.username.charAt(0).toUpperCase()}</div>
                <div>
                    <div class="text-primary fw-medium small">${u.username}</div>
                    <div class="text-muted" style="font-size: 0.7rem;">Active Collaborator</div>
                </div>
            </div>
            <button class="btn btn-sm btn-link text-danger p-1" title="Remove Collaborator" onclick="handleRemoveCollaborator(${u.id})">
                <i class="bi bi-person-x"></i>
            </button>
        </div>
    `).join('');
}

function renderPendingInvites(invites) {
    const container = document.getElementById('pendingList');
    const section = document.getElementById('pendingSection');
    
    document.getElementById('pendingCount').innerText = invites.length;

    if (invites.length === 0) {
        section.classList.add('d-none');
        return;
    }

    section.classList.remove('d-none');
    container.innerHTML = invites.map(i => `
        <div class="collab-item d-flex align-items-center justify-content-between border-warning border-opacity-10">
            <div class="d-flex align-items-center gap-3">
                <div class="avatar-circle bg-warning bg-opacity-20 text-warning">${i.receiver.username.charAt(0).toUpperCase()}</div>
                <div>
                    <div class="text-primary fw-medium small">${i.receiver.username}</div>
                    <div class="text-warning" style="font-size: 0.7rem;">Invitation Pending</div>
                </div>
            </div>
            <button class="btn btn-sm btn-link text-muted p-1" title="Cancel Invitation" onclick="handleCancelInvite(${i.id})">
                <i class="bi bi-x-circle"></i>
            </button>
        </div>
    `).join('');
}

/**
 * Action: Send new invitation
 */
async function handleSendInvite() {
    const input = document.getElementById('inviteUsernameInput');
    const username = input.value.trim();
    const feedback = document.getElementById('inviteFeedback');

    if (!username) {
        feedback.className = 'small mt-2 px-1 text-danger';
        feedback.innerText = 'Please enter a username';
        return;
    }

    try {
        const response = await fetch(`/api/invitations/tasks/${currentAssignmentTaskId}/invite`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username })
        });

        if (response.ok) {
            input.value = '';
            feedback.className = 'small mt-2 px-1 text-success';
            feedback.innerText = 'Invitation sent successfully!';
            await refreshCollaborationUI();
        } else {
            const error = await response.text();
            feedback.className = 'small mt-2 px-1 text-danger';
            feedback.innerText = 'Error: ' + (error || 'Failed to send invitation');
        }
    } catch (error) {
        console.error('Error sending invitation:', error);
        feedback.innerText = 'System error occurred';
    }
}

/**
 * Action: Cancel invitation
 */
async function handleCancelInvite(inviteId) {
    if (!confirm('Cancel this invitation?')) return;

    try {
        const response = await fetch(`/api/invitations/${inviteId}`, { method: 'DELETE' });
        if (response.ok) {
            await refreshCollaborationUI();
            showToast('success', 'Invitation cancelled');
        }
    } catch (error) {
        console.error('Error cancelling invite:', error);
    }
}

/**
 * Action: Remove collaborator
 */
async function handleRemoveCollaborator(userId) {
    if (!confirm('Remove this collaborator from the task?')) return;

    try {
        const response = await fetch(`/api/tasks/${currentAssignmentTaskId}/collaborators/${userId}`, { method: 'DELETE' });
        if (response.ok) {
            await refreshCollaborationUI();
            showToast('success', 'Collaborator removed');
        }
    } catch (error) {
        console.error('Error removing collaborator:', error);
    }
}

/**
 * UI Helper: Toast notification (assuming available in layout)
 */
function showToast(type, message) {
    // We assume the fragment 'toasts.html' provides a method, 
    // but we'll try a fallback if it doesn't exist
    if (window.pushNotification) {
        window.pushNotification(type, message);
    } else {
        console.log(`Toast: [${type}] ${message}`);
    }
}
