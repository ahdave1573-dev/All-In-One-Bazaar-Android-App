# Bazaar Ocean Calm UI/UX Design System

## Reusable Design System

- Use `#E8F4FD` for the app background, `#A8D5F0` for secondary panels, `#4A7FC1` for icons and secondary actions, `#1A56C4` for primary CTAs, `#0C3A8A` for readable dark text, and `#F5A623` only for CTA accents, key links, offer labels, ratings, and important highlights.
- Keep the app light-only. Avoid dark mode surfaces, heavy gradients, crowded sections, and decorative color noise.
- Use Poppins for the brand feel already present in the app. Recommended scale: 32-38sp hero headings, 20-24sp page titles, 16-18sp section titles, 14-16sp body text, and 11-13sp helper text.
- Use white or very pale blue cards with 18-24dp corner radius, 2-5dp elevation, and generous inner padding.
- Use primary buttons at 52-56dp height with 18-22dp radius, `#1A56C4` background, white text, and strong uppercase or semibold labels.
- Use secondary buttons with pale blue fill, blue border, and dark blue text. Use orange sparingly for links such as Forgot Password, Apply Coupon, Track Order, and discount badges.
- Use rounded input fields with pale blue fill, `#A8D5F0` outline, 54dp height, left icon, dark blue input text, and muted blue placeholder text.
- Use consistent mobile spacing: 24-28dp page padding, 16dp card gaps, 12dp row gaps, and 8dp micro spacing.
- Use subtle transitions only: fade, slide-up sheet, carousel swipe, button press alpha, and short loading motion.
- Bottom navigation should be soft and simple, with blue active state and orange only for the central cart CTA when needed.

## 1. Splash Screen

- Header structure: no app bar; full-screen brand presentation.
- Layout hierarchy: Ocean Calm background, soft abstract circles, centered white circular logo badge, app name, tagline, and bottom loading indicator.
- Button placement: no primary button during normal load; optional Skip only if load exceeds expected time.
- Input field style: none.
- Typography style: app name bold dark blue, tagline regular secondary blue.
- Navigation behavior: route returning users to Home and first-time users to Onboarding or Login.
- Color usage: preserve reference style with pale blue background and dark blue brand text.
- UX reasoning: keeps the first impression calm, branded, and premium without adding friction.

## 2. Onboarding Screens

- Header structure: top-right Skip text link.
- Layout hierarchy: large illustration, headline, short copy, progress dots, primary bottom CTA.
- Button placement: Next/Get Started button fixed near the bottom with safe-area spacing.
- Input field style: none.
- Typography style: bold 28-32sp heading, 14-15sp body copy.
- Navigation behavior: swipeable pages with skip and final Get Started action.
- Color usage: blue surfaces, white cards, orange only for active dot or one key highlight.
- UX reasoning: communicates value quickly while letting users skip.

## 3. Login Page

- Header structure: logo badge plus Bazaar wordmark.
- Layout hierarchy: Welcome Back hero, subtitle, rounded white form sheet.
- Button placement: Sign In after password, Google login below divider, Create Account at bottom.
- Input field style: email and password fields with icons, pale blue fill, rounded outline, visibility toggle.
- Typography style: large dark blue heading; orange only on Back and Forgot Password.
- Navigation behavior: Forgot Password opens reset flow; Create Account opens Register; successful login opens Home.
- Color usage: match provided reference exactly.
- UX reasoning: familiar form structure and large touch targets make sign-in fast and comfortable.

## 4. Register Page

- Header structure: back icon and compact brand mark.
- Layout hierarchy: Create Account title, helper text, form card, terms, CTA, login link.
- Button placement: Create Account button below terms checkbox.
- Input field style: name, email, phone, password, confirm password using the shared rounded input style.
- Typography style: 28-32sp title with readable 14sp helper text.
- Navigation behavior: back returns to Login; valid submit moves to OTP or Home.
- Color usage: blue CTA and form focus, orange only for Terms link.
- UX reasoning: keeps a longer form approachable by grouping fields clearly.

## 5. Forgot Password

- Header structure: back icon and Forgot Password title.
- Layout hierarchy: small reset illustration, instruction text, email/phone field, reset CTA.
- Button placement: Send Reset Link below the field.
- Input field style: single rounded input with mail/phone icon.
- Typography style: 24-28sp title and concise 14-15sp instructions.
- Navigation behavior: submit shows confirmation and Back to Login action.
- Color usage: blue CTA; orange only for important link text.
- UX reasoning: single-purpose layout reduces stress during account recovery.

## 6. Home Page

- Header structure: Bazaar wordmark or greeting, notification icon, profile/cart shortcut.
- Layout hierarchy: search bar, promo banner, categories, featured/top-rated product grid.
- Button placement: banner CTA as small orange accent; product actions inside product cards.
- Input field style: rounded search field with icon and placeholder.
- Typography style: 18-20sp section titles and compact product text.
- Navigation behavior: search opens Search; category opens filtered listing; product opens detail.
- Color usage: light background, white cards, blue highlights, orange only for offers.
- UX reasoning: supports quick discovery while keeping the first screen clean.

## 7. Categories Page

- Header structure: title and optional search/filter icon.
- Layout hierarchy: category cards in grid or vertical list with image/icon, name, and count.
- Button placement: entire card is tappable.
- Input field style: optional sticky search field.
- Typography style: 16sp category names, 12sp product count.
- Navigation behavior: tap opens category product list.
- Color usage: selected state uses blue outline or pale blue fill.
- UX reasoning: clear cards support browsing without clutter.

## 8. Product Details

- Header structure: back icon, wishlist icon, share icon.
- Layout hierarchy: image carousel, title, rating, price, variant controls, delivery info, description.
- Button placement: sticky bottom bar with Add to Cart and Buy Now.
- Input field style: rounded quantity stepper.
- Typography style: 22sp product title, 24sp bold price, compact helper labels.
- Navigation behavior: image swipe, wishlist toggle, cart badge update, Buy Now opens Checkout.
- Color usage: blue CTAs; orange for discount/rating highlight only.
- UX reasoning: purchase actions remain visible, improving conversion.

## 9. Cart Page

- Header structure: My Cart title with item count.
- Layout hierarchy: item cards, quantity controls, coupon row, order summary.
- Button placement: sticky Checkout button with total amount.
- Input field style: coupon field as rounded compact input with Apply action.
- Typography style: clear product names, dark blue totals, muted secondary labels.
- Navigation behavior: quantity changes update total instantly; remove shows undo snackbar.
- Color usage: orange only for coupon/discount messaging.
- UX reasoning: keeps price clarity and checkout access always visible.

## 10. Checkout Page

- Header structure: back icon and Checkout title.
- Layout hierarchy: delivery address, delivery method, payment method, order summary.
- Button placement: sticky Place Order CTA.
- Input field style: selection cards with radio/check indicators.
- Typography style: 18sp section titles and compact summary rows.
- Navigation behavior: edit address opens profile/address editor; payment opens selection sheet.
- Color usage: blue selected borders, orange for savings.
- UX reasoning: grouped checkout sections reduce anxiety and mistakes.

## 11. Order Success Screen

- Header structure: optional close icon only.
- Layout hierarchy: success icon, Order Placed title, order number, delivery estimate.
- Button placement: Track Order primary, Continue Shopping secondary.
- Input field style: none.
- Typography style: 28sp success title with readable support text.
- Navigation behavior: Track Order opens order detail; Continue Shopping opens Home.
- Color usage: blue success treatment with small orange celebratory accent.
- UX reasoning: confirms purchase and gives clear next steps.

## 12. Wishlist

- Header structure: Wishlist title, item count, search icon.
- Layout hierarchy: saved product grid/list with image, title, price, heart, Add button.
- Button placement: compact Add to Cart per item; Explore Products for empty state.
- Input field style: optional search/filter chips.
- Typography style: compact product names and bold prices.
- Navigation behavior: tap product opens details; unheart removes with undo snackbar.
- Color usage: active heart or sale label can use orange sparingly.
- UX reasoning: turns saved intent into easy purchase action.

## 13. User Profile

- Header structure: Profile title and settings icon.
- Layout hierarchy: avatar card, name/email, quick stats, account menu rows.
- Button placement: Edit Profile inside profile card.
- Input field style: none.
- Typography style: 22sp name, 14sp email, 15sp menu rows.
- Navigation behavior: menu rows open Orders, Addresses, Payments, Settings, and Help.
- Color usage: blue icons with white cards; orange only for rewards or important badges.
- UX reasoning: organizes account actions into predictable groups.

## 14. Edit Profile

- Header structure: back icon, Edit Profile title, optional Save text action.
- Layout hierarchy: avatar upload, personal fields, contact/address fields.
- Button placement: Save Changes fixed near bottom.
- Input field style: shared rounded inputs with validation states.
- Typography style: 14sp field labels in dark blue.
- Navigation behavior: save validates and returns to Profile with success snackbar.
- Color usage: blue focus states, orange only for important helper links.
- UX reasoning: stable inputs make profile editing predictable.

## 15. Notifications

- Header structure: Notifications title and Mark all read action.
- Layout hierarchy: grouped list by Today, Yesterday, Earlier.
- Button placement: contextual action link inside relevant cards.
- Input field style: none.
- Typography style: 15sp title, 13sp body, 11sp time.
- Navigation behavior: tap opens order, product, or promotion detail.
- Color usage: unread rows use pale blue; orange dot only for important unread alerts.
- UX reasoning: grouping and unread states improve scanning.

## 16. Search Page

- Header structure: back icon plus active search field.
- Layout hierarchy: search input, recent searches, popular categories, result grid/list.
- Button placement: clear icon inside input and filter button beside input.
- Input field style: large rounded search input.
- Typography style: compact result count and product card text.
- Navigation behavior: typing updates results; filters open bottom sheet; empty state shows suggestions.
- Color usage: blue focus border; orange only for deal labels.
- UX reasoning: recent and suggested searches recover from vague queries.

## 17. Settings

- Header structure: back icon and Settings title.
- Layout hierarchy: grouped cards for Account, Preferences, Notifications, Privacy, About.
- Button placement: toggles for binary settings; logout separated at bottom.
- Input field style: switches with blue active state.
- Typography style: 13sp group labels and 15sp setting rows.
- Navigation behavior: rows open detail screens; toggles update instantly with snackbar feedback.
- Color usage: blue icons and active switches; orange only for destructive or critical links.
- UX reasoning: grouped settings prevent a long page from feeling overwhelming.

## 18. Help & Support

- Header structure: back icon and Help & Support title.
- Layout hierarchy: help search, FAQ accordion, order issue shortcut, contact support card.
- Button placement: Chat with Support primary, Email Us secondary.
- Input field style: rounded help search input.
- Typography style: 15sp FAQ question and 14sp answer.
- Navigation behavior: FAQ expands inline; support opens chat/contact flow.
- Color usage: calm blue cards; orange only for urgent support links.
- UX reasoning: search-first support reduces effort while clear contact options build confidence.
