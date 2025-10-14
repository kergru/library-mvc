// 🔐 CSRF-Token aus Meta-Tags lesen
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

document.addEventListener('DOMContentLoaded', () => {
  const config = window.createUserConfig;

  const modalButton = document.getElementById('createUserModalButton');
  if (modalButton) {
    modalButton.addEventListener('click', () => submitUserForm('Modal'));
  }

  async function submitUserForm(suffix) {
    clearFieldErrors(suffix);
    const alertContainer = suffix === 'Modal'
        ? document.getElementById('modal-alert-container')
        : document.getElementById('alert-container');
    if (alertContainer) alertContainer.innerHTML = '';

    const data = {
      username: getValue(`username${suffix}`),
      firstName: getValue(`firstName${suffix}`),
      lastName: getValue(`lastName${suffix}`),
      email: getValue(`email${suffix}`),
      password: getValue(`password${suffix}`)
    };

    try {
      const response = await fetch('/library/rest/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [csrfHeader]: csrfToken
        },
        body: JSON.stringify(data)
      });

      if (response.ok) {
        showAlert('success', config.successMessage, suffix);
        resetForm(suffix);
        setTimeout(() => {
          const modalEl = document.getElementById('createUserModal');
          if (modalEl) {
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if (modalInstance) modalInstance.hide();
          }
        }, 800);
        return;
      }

      const error = await response.json().catch(() => ({}));
      if (response.status === 409) {
        const hints = error.hints || [];
        if (hints.includes('username')) {
          markFieldInvalid(`username${suffix}`);
        }
        if (hints.includes('email')) {
          markFieldInvalid(`email${suffix}`);
        }
        showAlert('danger', config.conflictMessage, suffix);
      } else {
        showAlert('danger', config.unknownErrorMessage, suffix);
      }
    } catch (e) {
      console.error('Netzwerkfehler:', e);
      showAlert('danger', config.networkErrorMessage, suffix);
    }
  }

  function getValue(id) {
    return document.getElementById(id)?.value.trim() || '';
  }

  function markFieldInvalid(id) {
    const el = document.getElementById(id);
    if (el) {
      el.classList.add('is-invalid');
    }
  }

  function clearFieldErrors(suffix) {
    const fields = ['username', 'firstName', 'lastName', 'email', 'password'];
    fields.forEach(field => {
      const el = document.getElementById(`${field}${suffix}`);
      if (el) el.classList.remove('is-invalid');
    });
  }

  function resetForm(suffix) {
    const fields = ['username', 'firstName', 'lastName', 'email', 'password'];
    fields.forEach(field => {
      const el = document.getElementById(`${field}${suffix}`);
      if (el) el.value = '';
      el?.classList.remove('is-invalid');
    });
  }

  function showAlert(type, message, suffix) {
    const container = suffix === 'Modal'
        ? document.getElementById('modal-alert-container')
        : document.getElementById('alert-container');

    if (!container) return;

    container.innerHTML = `
      <div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    `;
  }
});
