const DEFAULT_PRODUCER_API = (() => {
    const saved = localStorage.getItem('producerApi');
    if (saved) {
        return saved;
    }
    const { protocol, hostname, port } = window.location;
    if (port === '8081') {
        return `${protocol}//${hostname}:8080/api/orders`;
    }
    return `${protocol}//${hostname}:${port || (protocol === 'https:' ? '443' : '80')}/api/orders`;
})();

const state = {
    events: [],
    stompClient: null,
    producerApi: DEFAULT_PRODUCER_API
};

let toastTimer;

const elements = {
    product: document.getElementById('product'),
    price: document.getElementById('price'),
    orderId: document.getElementById('orderId'),
    producerEndpoint: document.getElementById('producer-endpoint'),
    form: document.getElementById('order-form'),
    lastOrderId: document.getElementById('last-order-id'),
    lastProduct: document.getElementById('last-order-product'),
    lastPrice: document.getElementById('last-order-price'),
    lastResult: document.getElementById('last-order-result'),
    lastStatus: document.getElementById('last-publish-status'),
    toast: document.getElementById('toast'),
    feedBody: document.getElementById('feed-body'),
    statCount: document.getElementById('stat-count'),
    statAverage: document.getElementById('stat-average'),
    connectionStatus: document.getElementById('connection-status'),
    btnRandomProduct: document.getElementById('btn-random-product'),
    btnRandomOrder: document.getElementById('btn-random-order'),
    btnRefresh: document.getElementById('btn-refresh')
};

elements.producerEndpoint.value = state.producerApi;

const PRODUCT_PRESETS = [
    'Gaming Laptop',
    'Noise Cancelling Headphones',
    'Mechanical Keyboard',
    '4K Monitor',
    'Smart Speaker',
    'Ergonomic Chair',
    'Drone Pro X',
    'Portable SSD 2TB',
    'VR Headset',
    'Action Camera'
];

function showToast(type, message) {
    if (toastTimer) {
        clearTimeout(toastTimer);
    }
    elements.toast.textContent = message;
    elements.toast.className = `toast ${type}`;
    elements.toast.classList.remove('hidden');
    toastTimer = setTimeout(() => elements.toast.classList.add('hidden'), 4000);
}

function setConnectionStatus(text, tone = 'info') {
    elements.connectionStatus.textContent = text;
    elements.connectionStatus.className = `status-pill ${tone}`;
}

function renderStats() {
    const latest = state.events[0];
    elements.statCount.textContent = latest?.count ?? 0;
    elements.statAverage.textContent = latest?.average?.toFixed(2) ?? '0.00';
}

function renderFeed() {
    if (!state.events.length) {
        elements.feedBody.innerHTML = '<tr><td colspan="6" class="muted">No events received yet.</td></tr>';
        renderStats();
        return;
    }

    const rows = state.events.map(event => `
        <tr>
            <td>${formatTimestamp(event.timestamp)}</td>
            <td>${event.orderId}</td>
            <td>${event.product}</td>
            <td>$${Number(event.price).toFixed(2)}</td>
            <td><span class="status-pill-small ${event.status === 'PROCESSED' ? 'ok' : 'fail'}">${event.status}</span></td>
            <td>${event.message ?? ''}</td>
        </tr>
    `).join('');

    elements.feedBody.innerHTML = rows;
    renderStats();
}

function upsertEvent(event) {
    state.events = [event, ...state.events].slice(0, 100);
    renderFeed();
}

function formatTimestamp(value) {
    if (!value) {
        return '—';
    }
    const date = new Date(value);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

async function loadInitialFeed() {
    try {
        const response = await fetch('/api/feed');
        if (!response.ok) {
            throw new Error(`Feed request failed with ${response.status}`);
        }
        const data = await response.json();
        state.events = data;
        renderFeed();
        if (data.length) {
            showToast('info', `Loaded ${data.length} historical events.`);
        }
    } catch (error) {
        console.error('Feed error', error);
        showToast('error', 'Unable to load historical feed.');
    }
}

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const brokerURL = `${protocol}://${window.location.host}/ws-orders`;

    const { Client } = window.StompJs || {};
    if (!Client) {
        console.error('STOMP library missing');
        showToast('error', 'STOMP library failed to load.');
        return;
    }

    state.stompClient = new Client({
        brokerURL,
        reconnectDelay: 5000,
        onConnect: () => {
            setConnectionStatus('Live', 'success');
            state.stompClient.subscribe('/topic/orders', message => {
                try {
                    const payload = JSON.parse(message.body);
                    upsertEvent(payload);
                } catch (error) {
                    console.error('Message parse error', error);
                }
            });
        },
        onStompError: frame => {
            console.error('Broker error', frame.headers['message']);
            setConnectionStatus('Broker error', 'danger');
            showToast('error', 'Broker reported an error. Check consumer logs.');
        },
        onWebSocketClose: () => {
            setConnectionStatus('Reconnecting…', 'warning');
        }
    });

    state.stompClient.activate();
}

function randomProduct() {
    return PRODUCT_PRESETS[Math.floor(Math.random() * PRODUCT_PRESETS.length)];
}

function randomPrice() {
    return (Math.random() * (800 - 50) + 50).toFixed(2);
}

function setLastPublish(response, statusClass = 'success', label = 'Published') {
    elements.lastOrderId.textContent = response?.orderId ?? '—';
    elements.lastProduct.textContent = response?.product ?? '—';
    elements.lastPrice.textContent = response?.price ? `$${Number(response.price).toFixed(2)}` : '—';
    elements.lastResult.textContent = label;
    elements.lastStatus.textContent = label;
    elements.lastStatus.className = `badge ${statusClass}`;
}

async function publishOrder(payload) {
    try {
        const response = await fetch(state.producerApi, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const body = await response.json();
        if (!response.ok) {
            throw new Error(body?.details || body?.message || 'Failed to publish order');
        }
        setLastPublish(body, 'success', 'Published');
        showToast('success', `Order ${body.orderId} published.`);
    } catch (error) {
        console.error('Publish failed', error);
        setLastPublish(null, 'error', 'Failed');
        elements.lastResult.textContent = error.message;
        showToast('error', error.message);
    }
}

// Event listeners

elements.form.addEventListener('submit', event => {
    event.preventDefault();
    const payload = {
        product: elements.product.value.trim(),
        price: Number(elements.price.value),
        orderId: elements.orderId.value.trim() || null
    };
    publishOrder(payload);
});

elements.btnRandomProduct.addEventListener('click', () => {
    elements.product.value = randomProduct();
});

elements.btnRandomOrder.addEventListener('click', () => {
    elements.product.value = randomProduct();
    elements.price.value = randomPrice();
    elements.orderId.value = '';
});

elements.btnRefresh.addEventListener('click', loadInitialFeed);

elements.producerEndpoint.addEventListener('change', event => {
    state.producerApi = event.target.value.trim();
    localStorage.setItem('producerApi', state.producerApi);
    showToast('info', 'Updated producer endpoint.');
});

// Bootstrap
setConnectionStatus('Connecting…');
loadInitialFeed();
connectWebSocket();

if (!elements.product.value) {
    elements.product.value = PRODUCT_PRESETS[0];
}
if (!elements.price.value) {
    elements.price.value = randomPrice();
}
