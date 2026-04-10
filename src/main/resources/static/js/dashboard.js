// src/main/resources/static/js/dashboard.js

let qty = 1;
const pricePerCredit = 75;

function changeQty(change) {
    // Change the quantity, but prevent it from going below 1
    qty += change;
    if (qty < 1) qty = 1;

    // Update the hidden input for Stripe
    document.getElementById('creditQuantity').value = qty;

    // Update the visual number between the + and -
    document.getElementById('displayQty').innerText = qty;

    // Calculate the new total and update the button text
    const total = qty * pricePerCredit;
    const btnText = qty === 1
        ? `Purchase 1 Credit for $${total}`
        : `Purchase ${qty} Credits for $${total}`;

    document.getElementById('purchaseBtnText').innerText = btnText;
}