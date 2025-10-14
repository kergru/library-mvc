document.addEventListener('DOMContentLoaded', () => {
  const card = document.querySelector('.book-details');
  const isbn = card.getAttribute('data-isbn');

  const borrowButton = document.getElementById('borrow-button');
  const alertContainer = document.getElementById('loan-alert-container');
  const badgeAvailable = document.getElementById('badge-available');
  const badgeUnavailable = document.getElementById('badge-unavailable');

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  if (borrowButton) {
    borrowButton.addEventListener('click', async () => {
      alertContainer.innerHTML = '';

      try {
        const response = await fetch(`/library/rest/me/borrowBook/${isbn}`, {
          method: 'POST',
          headers: {
            [csrfHeader]: csrfToken
          }
        });

        if (response.ok) {
          alertContainer.innerHTML = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
              Buch wurde erfolgreich ausgeliehen.
              <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
          `;

          if (badgeAvailable) badgeAvailable.style.display = 'none';
          if (badgeUnavailable) {
            badgeUnavailable.style.display = 'inline';
          } else {
            const newBadge = document.createElement('span');
            newBadge.id = 'badge-unavailable';
            newBadge.className = 'badge bg-danger';
            newBadge.textContent = 'Ausgeliehen';
            badgeAvailable?.parentNode.appendChild(newBadge);
          }

          borrowButton.disabled = true;
          borrowButton.classList.remove('btn-primary');
          borrowButton.classList.add('btn-secondary');
          borrowButton.textContent = 'Nicht verfügbar';
        } else if (response.status === 409) {
          alertContainer.innerHTML = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
              Dieses Buch ist bereits ausgeliehen.
              <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
          `;
        } else {
          alertContainer.innerHTML = `
            <div class="alert alert-warning alert-dismissible fade show" role="alert">
              Ein unbekannter Fehler ist aufgetreten.
              <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
          `;
        }
      } catch (e) {
        console.error(e);
        alertContainer.innerHTML = `
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Fehler beim Verbinden mit dem Server.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
          </div>
        `;
      }
    });
  }
});
