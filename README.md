# \# Spliz — FairShare Intelligence

# \### No Awkward Money Talks. Split Expenses, Not Friendships.

# 

# \*\*Project Owners:\*\* Tulsi and Kavya Verma

# \*\*Category:\*\* Full-Stack Web Application

# \*\*Purpose:\*\* College Project Evaluation

# \*\*Architecture:\*\* Spring Boot 3 (Java 21+) · PostgreSQL 16 (Docker) · Vanilla HTML / CSS / JavaScript

# 

# \---

# 

# \## 1. Overview

# 

# \*\*Spliz\*\* is a shared-expense manager for student trips, roommates, meals and group activities. It records who paid, works out each person's share, shows who owes whom, and settles everything with as few payments as possible.

# 

# Beyond a basic splitter, Spliz adds a decision-support layer: a \*\*Fairness Simulator\*\* to preview split changes before saving them, a \*\*Settlement Optimizer\*\* that minimises the number of transfers, \*\*receipt scanning\*\* to speed up expense entry, and \*\*UPI QR payments\*\* for settling up.

# 

# \### Key Features

# 

# | Feature | What it does |

# |---|---|

# | \*\*Friends \& Professional modes\*\* | One toggle switches the whole interface between a playful "friends" look and wording, and a clean professional one. Both have light and dark themes. Only the presentation changes, never the calculations. |

# | \*\*Groups \& members\*\* | Create groups, add and edit members (with UPI IDs), and delete a group after a confirmation dialog. |

# | \*\*Expense management\*\* | Record, edit and delete shared expenses with equal or custom splits. |

# | \*\*Fairness Simulator\*\* | Preview how a proposed split changes everyone's share, then apply it only if it looks right. Nothing is saved until you apply. |

# | \*\*Settlement Optimizer\*\* | Exact minimal-transfer search when 8 or fewer members have a non-zero balance, and a greedy strategy otherwise. |

# | \*\*Settle Up \& cancel mutual splits\*\* | Detects offsetting expenses that cancel each other out and settles the rest in one step. |

# | \*\*UPI payments \& QR\*\* | Start a UPI payment for a suggested transfer, show a QR code, and confirm the payment once it is done. |

# | \*\*Receipt scanner\*\* | Upload or capture a receipt; text is read in the browser with OCR (Tesseract.js), and QR codes in the image are read with jsQR, to help fill in an expense. Always review the values before saving. |

# | \*\*Balances \& activity history\*\* | Per-member net balances and a readable log of what changed and why. |

# 

# \---

# 

# \## 2. System Architecture

# 

# ```mermaid

# flowchart TD

# &#x20;   U\[User Browser] --> UI\[Frontend: HTML, CSS, JavaScript<br/>Port 3000]

# &#x20;   UI -->|REST / JSON| BE\[Spring Boot API<br/>Port 8080]

# &#x20;   BE --> DB\[(PostgreSQL 16 in Docker<br/>Port 5433)]

# &#x20;   BE --> SIM\[Fairness Simulator]

# &#x20;   BE --> SETTLE\[Settlement Optimizer]

# &#x20;   UI --> OCR\[Receipt Scanner - Tesseract.js]

# &#x20;   UI --> QR\[QR Generator - UPI]

# ```

# 

# The frontend is plain static files. It talks to the Spring Boot backend over REST. The backend handles groups, members, expenses, balances and settlements, and stores everything in PostgreSQL. On the first start it also seeds a demo group ("Goa Trip 2026") if the database is empty.

# 

# \---

# 

# \## 3. Technology Stack

# 

# | Layer | Technologies |

# |---|---|

# | \*\*Frontend\*\* | HTML5, CSS3 (custom theme system), Vanilla JavaScript ES modules, Tesseract.js, jsQR |

# | \*\*Backend\*\* | Java 21+, Spring Boot 3.4, Spring Data JPA, Hibernate, Jakarta Bean Validation |

# | \*\*Database\*\* | PostgreSQL 16 (Docker Compose); H2 in-memory database for tests |

# | \*\*Testing\*\* | JUnit 5, Spring Boot Test |

# | \*\*Tooling\*\* | Maven Wrapper, Docker Desktop, Git, VS Code |

# 

# \---

# 

# \## 4. Project Structure

# 

# ```

# Spliz/

# ├── backend/                 Spring Boot application

# │   └── src/main/java/com/fairshare/

# │       ├── controller/      REST endpoints

# │       ├── service/         Split, settlement, fairness and UPI logic

# │       ├── entity/          JPA entities

# │       ├── enums/           Enumerations (split methods, statuses)

# │       ├── exception/       Error handling

# │       ├── repository/      Spring Data repositories

# │       ├── dto/             Request and response objects

# │       └── config/          CORS and demo-data seeding

# ├── frontend/                Static web app

# │   ├── index.html

# │   ├── css/                 Theme system (friends / professional, light / dark)

# │   └── js/                  App logic, API client, receipt scanner, QR generator

# ├── docker-compose.yml       PostgreSQL + pgAdmin

# ├── .env.example             Environment template

# └── README.md

# ```

# 

# \---

# 

# \## 5. Quick Start

# 

# \### Prerequisites

# \- \*\*Java 21+\*\*

# \- \*\*Docker Desktop\*\* (must be running)

# \- \*\*Git\*\*

# \- \*\*Python 3\*\* (only for the small frontend web server)

# 

# \### Step 1: Get the code

# ```bash

# git clone https://github.com/itsTulsi/Spliz.git

# cd Spliz

# ```

# 

# \### Step 2: Configure the environment

# ```powershell

# \# Windows PowerShell

# Copy-Item .env.example .env

# ```

# ```bash

# \# macOS / Linux

# cp .env.example .env

# ```

# 

# > \*\*Important:\*\* open `.env` and set `DB\_PORT=5433`.

# > The backend connects to PostgreSQL on port \*\*5433\*\*. If `.env` keeps `DB\_PORT=5432`, the database starts on the wrong port and the backend fails with `Connection to 127.0.0.1:5433 refused`.

# 

# Never commit `.env` or any real passwords to Git.

# 

# \### Step 3: Start the database

# ```bash

# docker compose up -d

# ```

# \- PostgreSQL runs on `localhost:5433` (database `fairshare\_db`, user `fairshare\_user`).

# \- Optional pgAdmin runs at `http://localhost:5050`.

# \- Check it is ready: `docker ps` should show `fairshare-postgres` as \*\*healthy\*\*.

# 

# If Docker reports that a container name is already in use, remove the old one and retry:

# ```bash

# docker rm -f fairshare-postgres fairshare-pgadmin

# docker compose up -d

# ```

# 

# \### Step 4: Start the backend

# ```powershell

# cd backend

# .\\mvnw.cmd spring-boot:run        # Windows

# ```

# ```bash

# ./mvnw spring-boot:run            # macOS / Linux

# ```

# Wait for `Started FairshareBackendApplication`. The API is at `http://localhost:8080`.

# 

# \### Step 5: Start the frontend

# In a \*\*new terminal\*\*, from the project root:

# ```bash

# py -m http.server 3000 --directory frontend        # Windows

# python3 -m http.server 3000 --directory frontend   # macOS / Linux

# ```

# 

# Open \*\*http://localhost:3000\*\* and press `Ctrl+Shift+R` once to load the latest styles. Keep both the backend and frontend terminals running.

# 

# \### Step 6: Run the automated tests

# ```powershell

# cd backend

# .\\mvnw.cmd test        # Windows

# ```

# ```bash

# cd backend

# ./mvnw test           # macOS / Linux

# ```

# 

# \### Stopping everything

# Press `Ctrl+C` in the backend and frontend terminals, then (optional) `docker compose down`.

# 

# \---

# 

# \## 6. Core Modules

# 

# \### Friends and Professional modes

# The toggle in the top bar switches the wording, fonts and colours of the whole interface.

# 

# \- \*\*Friends mode:\*\* playful wording ("The Crew", "Who Owes Who"), rounded hand-lettered headings and chunky cards. Light theme is sky blue and white with orange / red headings. Dark theme is deep navy.

# \- \*\*Professional mode:\*\* clear, formal wording ("Expense Groups", "Balances and Settle Up"). Light theme is fresh ivory with espresso brown and brass accents. Dark theme is rich espresso brown with gold.

# 

# The separate \*\*Dark / Light\*\* button changes the theme within the current mode. Your choices are remembered in the browser.

# 

# \### Fairness Simulator

# Pick an expense, propose a different split, and see each person's old and new share side by side. The preview is non-destructive: the saved expense changes only when you apply the new split.

# 

# \### Settlement Optimizer

# Turns the group's net balances into the smallest practical set of payments. When 8 or fewer members have a non-zero balance, it searches for the exact minimum number of transfers. Otherwise it uses a greedy heuristic. The algorithm used is shown next to the suggestions.

# 

# \### Settle Up

# Finds mutual, offsetting expenses that cancel each other out, shows a preview, and settles the group in one confirmed step.

# 

# \### UPI payments and QR

# For a suggested transfer you can start a UPI payment, show a QR code for the payee, and then confirm that the payment was completed. Spliz records the outcome; it does not move money itself.

# 

# \### Receipt scanner

# Choose or capture a receipt image. Text is extracted in the browser using OCR to help pre-fill an expense. OCR can misread values, so always review the amount and description before saving.

# 

# \---

# 

# \## 7. API Endpoints

# 

# Base URL: `http://localhost:8080/api`

# 

# \### Groups and members

# | Method | Endpoint | Purpose |

# |---|---|---|

# | `GET` | `/groups` | List groups |

# | `POST` | `/groups` | Create a group |

# | `GET` | `/groups/{id}` | Get one group |

# | `PUT` | `/groups/{id}` | Update a group |

# | `DELETE` | `/groups/{id}` | Delete a group |

# | `GET` | `/groups/{id}/members` | List members |

# | `POST` | `/groups/{id}/members` | Add a member |

# | `PUT` | `/groups/{id}/members/{memberId}` | Edit a member |

# 

# \### Expenses and fairness

# | Method | Endpoint | Purpose |

# |---|---|---|

# | `GET` | `/groups/{id}/expenses` | List expenses |

# | `POST` | `/groups/{id}/expenses` | Record an expense |

# | `GET` | `/expenses/{id}` | Get one expense |

# | `PUT` | `/expenses/{id}` | Edit an expense |

# | `DELETE` | `/expenses/{id}` | Delete an expense |

# | `POST` | `/expenses/{id}/preview-split` | Preview a proposed split (nothing saved) |

# | `PUT` | `/expenses/{id}/apply-split` | Apply a split change |

# 

# \### Balances, settlements and activity

# | Method | Endpoint | Purpose |

# |---|---|---|

# | `GET` | `/groups/{id}/balances` | Member net balances |

# | `GET` | `/groups/{id}/settlements/suggestions` | Optimised payment suggestions |

# | `GET` | `/groups/{id}/settlements` | List recorded settlements |

# | `GET` | `/groups/{id}/settlements/{settlementId}` | Get one settlement |

# | `POST` | `/groups/{id}/settlements` | Record a settlement |

# | `POST` | `/groups/{id}/settlements/initiate` | Start a UPI payment |

# | `POST` | `/groups/{id}/settlements/{settlementId}/confirm` | Confirm a payment |

# | `GET` | `/groups/{id}/settle-up/preview` | Preview "settle up and cancel mutual splits" |

# | `POST` | `/groups/{id}/settle-up` | Run settle up |

# | `GET` | `/groups/{id}/activity` | Activity history |

# 

# \---

# 

# \## 8. Financial Integrity

# 

# \- All money is stored as \*\*integer paise\*\* (₹1 = 100 paise). No floating-point arithmetic is used for money.

# \- The shares of an expense always add up to the expense total.

# \- The net balances of a group always add up to zero. The app shows a "Zero-Sum Verified" badge when this holds.

# \- Settlement suggestions are shown for review before anything is recorded.

# \- Split, settlement, optimizer and UPI logic are covered by JUnit tests (`./mvnw test`).

# 

# \---

# 

# \## 9. Demonstration Flow

# 

# 1\. Open Spliz. The demo group "Goa Trip 2026" is already there.

# 2\. Switch between \*\*Friends\*\* and \*\*Professional\*\* mode, and between \*\*Dark\*\* and \*\*Light\*\*.

# 3\. Open the group, add a member and record a shared expense.

# 4\. Open the \*\*receipt scanner\*\* and show the scan-to-expense flow.

# 5\. Use the \*\*Fairness Simulator\*\* to preview a different split, without saving it.

# 6\. Open \*\*Balances and Settle Up\*\*, check the zero-sum badge and view the optimised payment suggestions.

# 7\. Start a \*\*UPI payment\*\* to show the QR code.

# 8\. Open the \*\*activity history\*\* to show the explained changes.

# 9\. Show the \*\*group deletion confirmation\*\* dialog (cancel it so the demo data stays).

# 

# \---

# 

# \## 10. Known Limitations

# 

# \- \*\*No user accounts yet.\*\* There is no login, so every group is visible to anyone who can open the app. User accounts, per-user data and friends are planned next.

# \- The frontend and API are meant to run locally for demonstration; CORS is open for development.

# \- Receipt scanning depends on OCR accuracy and image quality, so extracted values must be checked.

# \- Spliz records payments but does not process real money.

# 

# \---

# 

# \## 11. Project Goals

# 

# Spliz aims to make shared expenses easier to manage through transparent calculations, understandable settlement suggestions, and an interface that suits both a group of friends and a professional setting.

# 

# \*\*Spliz — Split expenses, not friendships.\*\*

