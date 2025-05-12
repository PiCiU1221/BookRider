# User Mobile App - v1.0

## Overview

The **User Mobile App** is a client application built specifically for the **BookRider** backend. It provides a structured interface for users to interact with the system’s API, allowing them to:

- Browse our selection of books available for borrowing.
- Filter books based on various factors, like categories or libraries.
- Add selected books to their shopping cart and manage their state.
- Check prices for deliveries or returns.
- Track borrowed and returned books.
- Create in-person or with our delivery returns.

This application was designed to integrate with our backend, ensuring that users can interact with the backend through a standardized and consistent interface.

## 🛠️ **Tech Stack**

- **React Native**
- **Expo**
- **NativeWind**

## 🔄 **WebSocket Integration**

The app uses **WebSockets** to maintain a real-time connection between the server and client devices.

Refresh notifications are automatically pushed when there are updates to the **order status**, **rental return status**, or the currently active **order category** (*Pending*, *In Realization*, or *Delivered*).  
Only the visible category is refreshed, keeping the user’s current view up-to-date without needing to manually reload the app.

## 📸 Screenshots

<table>
  <tr>
    <td><img src="docs/screenshots/main-screen.png" alt="main-screen" width="300"/></td>
    <td><img src="docs/screenshots/book-preview.png" alt="book-preview" width="300"/></td>
  </tr>
  <tr>
    <td>Main screen</td>
    <td>Book preview</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/book-filtering.png" alt="book-filtering" width="300"/></td>
    <td><img src="docs/screenshots/book-querying.png" alt="book-querying" width="300"/></td>
  </tr>
  <tr>
    <td>Book filtering</td>
    <td>Book querying</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/shopping-cart.png" alt="shopping-cart" width="300"/></td>
    <td><img src="docs/screenshots/account-details.png" alt="account-details" width="300"/></td>
  </tr>
  <tr>
    <td>Shopping cart</td>
    <td>Account details</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/rental-return-preview.png" alt="rental-return-preview" width="300"/></td>
    <td><img src="docs/screenshots/return-confirmation-preview.png" alt="return-confirmation-preview" width="300"/></td>
  </tr>
  <tr>
    <td>Rental return preview</td>
    <td>Return confirmation preview</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/rental-return-scan-id-view.png" alt="return-scan-id-view" width="300"/></td>
    <td><img src="docs/screenshots/rentals-screen.png" alt="rentals-view" width="300"/></td>
  </tr>
  <tr>
    <td>Rental return scan ID view</td>
    <td>Rentals view</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/order-history-details.png" alt="order-history-details" width="300"/></td>
    <td><img src="docs/screenshots/order-history.png" alt="order-history-view" width="300"/></td>
  </tr>
  <tr>
    <td>Order history details</td>
    <td>Order history view</td>
  </tr>
</table>

<br>

<table>
  <tr>
    <td><img src="docs/screenshots/login-screen.png" alt="login-screen" width="300"/></td>
    <td><img src="docs/screenshots/register-screen.png" alt="register-screen" width="300"/></td>
  </tr>
  <tr>
    <td>Login screen</td>
    <td>Register screen</td>
  </tr>
</table>
