const user = Session.get();
if (!user) {
  window.location.href = 'index.html';
}
document.getElementById('who-name').textContent = user.fullName;
document.getElementById('logout-btn').addEventListener('click', () => {
  Session.clear();
  window.location.href = 'index.html';
});

// ---- Tabs -----------------------------------------------------------

const tabBrowse = document.getElementById('tab-browse');
const tabBookings = document.getElementById('tab-bookings');
const panelBrowse = document.getElementById('panel-browse');
const panelBookings = document.getElementById('panel-bookings');

tabBrowse.addEventListener('click', () => switchTab('browse'));
tabBookings.addEventListener('click', () => switchTab('bookings'));

function switchTab(which) {
  const isBrowse = which === 'browse';
  tabBrowse.classList.toggle('active', isBrowse);
  tabBookings.classList.toggle('active', !isBrowse);
  panelBrowse.hidden = !isBrowse;
  panelBookings.hidden = isBrowse;
  if (!isBrowse) loadBookings();
}

// ---- Reference data (locations, car types) ---------------------------

let locations = [];
let carTypes = [];

async function loadReferenceData() {
  [locations, carTypes] = await Promise.all([
    apiRequest('/api/locations'),
    apiRequest('/api/car-types'),
  ]);

  const typeFilter = document.getElementById('filter-type');
  carTypes.forEach(t => typeFilter.add(new Option(t.typeName, t.carTypeId)));

  const locFilter = document.getElementById('filter-location');
  locations.forEach(l => locFilter.add(new Option(l.name, l.locationId)));

  const pickupSel = document.getElementById('pickup-location');
  const dropoffSel = document.getElementById('dropoff-location');
  locations.forEach(l => {
    pickupSel.add(new Option(l.name, l.locationId));
    dropoffSel.add(new Option(l.name, l.locationId));
  });
}

document.getElementById('filter-type').addEventListener('change', loadVehicles);
document.getElementById('filter-location').addEventListener('change', loadVehicles);

// ---- Vehicle grid -----------------------------------------------------

async function loadVehicles() {
  const carTypeId = document.getElementById('filter-type').value;
  const locationId = document.getElementById('filter-location').value;
  const params = new URLSearchParams();
  if (carTypeId) params.set('carTypeId', carTypeId);
  if (locationId) params.set('locationId', locationId);

  const vehicles = await apiRequest('/api/vehicles?' + params.toString());
  const grid = document.getElementById('vehicle-grid');
  const empty = document.getElementById('vehicle-empty');

  grid.innerHTML = '';
  empty.hidden = vehicles.length > 0;

  vehicles.forEach(v => {
    const card = document.createElement('div');
    card.className = 'vehicle-card';
    card.innerHTML = `
      <div class="swatch">
        <span class="type">${v.carType}</span>
        <span class="plate">${v.licensePlate}</span>
      </div>
      <div class="body">
        <h3>${v.year} ${v.make} ${v.model}</h3>
        <div class="meta">${locationName(v.locationId)}</div>
        <div class="price">${money(v.dailyRate)} <small>/ day</small></div>
        <button class="btn btn-accent" data-book="${v.vehicleId}">Book this car</button>
      </div>`;
    grid.appendChild(card);
  });

  grid.querySelectorAll('[data-book]').forEach(btn => {
    btn.addEventListener('click', () => openBookingModal(vehicles.find(v => v.vehicleId == btn.dataset.book)));
  });
}

function locationName(id) {
  const loc = locations.find(l => l.locationId === id);
  return loc ? loc.name : '';
}

// ---- Booking modal ------------------------------------------------------

const overlay = document.getElementById('modal-overlay');
const bookingForm = document.getElementById('booking-form');
const pickupDateInput = document.getElementById('pickup-date');
const returnDateInput = document.getElementById('return-date');
let activeVehicle = null;

function openBookingModal(vehicle) {
  activeVehicle = vehicle;
  document.getElementById('modal-title').textContent = `Book the ${vehicle.year} ${vehicle.make} ${vehicle.model}`;
  document.getElementById('modal-sub').textContent = `${money(vehicle.dailyRate)} per day · ${vehicle.carType}`;
  document.getElementById('modal-alert').innerHTML = '';

  const today = new Date().toISOString().split('T')[0];
  pickupDateInput.min = today;
  pickupDateInput.value = today;
  returnDateInput.min = today;
  returnDateInput.value = '';

  document.getElementById('pickup-location').value = vehicle.locationId;
  document.getElementById('dropoff-location').value = vehicle.locationId;

  updateEstimate();
  overlay.hidden = false;
}

function closeModal() {
  overlay.hidden = true;
  activeVehicle = null;
}
document.getElementById('modal-cancel').addEventListener('click', closeModal);
overlay.addEventListener('click', (e) => { if (e.target === overlay) closeModal(); });

function updateEstimate() {
  const estimateEl = document.getElementById('estimate');
  if (!activeVehicle || !pickupDateInput.value || !returnDateInput.value) {
    estimateEl.textContent = '—';
    return;
  }
  const days = Math.max(1, (new Date(returnDateInput.value) - new Date(pickupDateInput.value)) / 86400000);
  estimateEl.textContent = money(days * activeVehicle.dailyRate) + ` (${days} day${days === 1 ? '' : 's'})`;
}
pickupDateInput.addEventListener('change', () => {
  returnDateInput.min = pickupDateInput.value;
  updateEstimate();
});
returnDateInput.addEventListener('change', updateEstimate);

bookingForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const confirmBtn = document.getElementById('modal-confirm');
  confirmBtn.disabled = true;
  document.getElementById('modal-alert').innerHTML = '';

  try {
    await apiRequest('/api/bookings', {
      method: 'POST',
      body: JSON.stringify({
        customerId: user.customerId,
        vehicleId: activeVehicle.vehicleId,
        pickupLocationId: Number(document.getElementById('pickup-location').value),
        dropoffLocationId: Number(document.getElementById('dropoff-location').value),
        startDate: pickupDateInput.value,
        endDate: returnDateInput.value,
      }),
    });
    closeModal();
    await loadVehicles();
    switchTab('bookings');
  } catch (err) {
    document.getElementById('modal-alert').innerHTML = `<div class="alert alert-error">${err.message}</div>`;
  } finally {
    confirmBtn.disabled = false;
  }
});

// ---- My bookings --------------------------------------------------------

async function loadBookings() {
  const bookings = await apiRequest('/api/bookings?customerId=' + user.customerId);
  const table = document.getElementById('bookings-table');
  const body = document.getElementById('bookings-body');
  const empty = document.getElementById('bookings-empty');

  table.hidden = bookings.length === 0;
  empty.hidden = bookings.length > 0;
  body.innerHTML = '';

  bookings.forEach(b => {
    const row = document.createElement('tr');
    const canCancel = b.status === 'CONFIRMED' || b.status === 'PENDING';
    row.innerHTML = `
      <td>#${b.bookingId}</td>
      <td>Vehicle #${b.vehicleId}</td>
      <td>${formatDate(b.startDate)} – ${formatDate(b.endDate)}</td>
      <td>${money(b.totalCost)}</td>
      <td><span class="status-pill status-${b.status}">${b.status}</span></td>
      <td>${canCancel ? `<button class="btn-text" data-cancel="${b.bookingId}">Cancel</button>` : ''}</td>`;
    body.appendChild(row);
  });

  body.querySelectorAll('[data-cancel]').forEach(btn => {
    btn.addEventListener('click', async () => {
      btn.disabled = true;
      try {
        await apiRequest('/api/bookings/' + btn.dataset.cancel, { method: 'DELETE' });
        await loadBookings();
        await loadVehicles();
      } catch (err) {
        alert(err.message);
        btn.disabled = false;
      }
    });
  });
}

// ---- Init -----------------------------------------------------------

(async function init() {
  await loadReferenceData();
  await loadVehicles();
})();
