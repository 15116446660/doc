from playwright.sync_api import sync_playwright, expect, Page
import time

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    try:
        # 1. Navigate to the app and log in
        page.goto("http://localhost:5173/")

        expect(page).to_have_url("http://localhost:5173/login", timeout=10000)

        page.get_by_placeholder("用户名").fill("testuser")
        page.get_by_placeholder("密码").fill("password123")
        page.get_by_role("button", name="登录").click()

        # 2. Navigate to the Task Management page
        expect(page).to_have_url("http://localhost:5173/dashboard", timeout=10000)

        # Click the '评审系统' menu item to reveal the submenu
        page.get_by_role("menuitem", name="评审系统").click()

        # Click the '任务管理' submenu item
        page.get_by_role("menuitem", name="任务管理").click()

        expect(page).to_have_url("http://localhost:5173/review-system/tasks", timeout=10000)

        # 3. Verify the initial task list
        expect(page.get_by_role("heading", name="评审任务管理")).to_be_visible()
        expect(page.get_by_text("Task 1")).to_be_visible()
        time.sleep(1) # wait for render
        page.screenshot(path="jules-scratch/verification/01_task_list.png")

        # 4. Create a new task
        page.get_by_role("button", name="创建任务").click()

        dialog_title = page.get_by_role("heading", name="创建评审任务")
        expect(dialog_title).to_be_visible()

        page.get_by_label("任务名称").fill("My New Test Task")
        page.get_by_label("任务描述").fill("This is a description for the new test task.")
        page.get_by_role("button", name="创建").last.click()

        # 5. Verify the new task is in the list
        expect(page.get_by_text("My New Test Task")).to_be_visible()
        time.sleep(1) # wait for render
        page.screenshot(path="jules-scratch/verification/02_new_task_visible.png")

        # 6. Navigate to the workspace for the new task
        new_task_row = page.get_by_role("row").filter(has_text="My New Test Task")
        new_task_row.get_by_role("button", name="查看").click()

        # 7. Verify the workspace
        expect(page).to_have_url(lambda url: "/review-system/workspace/" in url, timeout=10000)
        expect(page.get_by_role("heading", name="My New Test Task Details")).to_be_visible()

        issues_tab = page.get_by_role("tab", name="问题清单")
        expect(issues_tab).to_be_visible()
        issues_tab.click()

        expect(page.get_by_text("Issue 1")).to_be_visible()
        time.sleep(1) # wait for render
        page.screenshot(path="jules-scratch/verification/verification.png")

        print("Verification script completed successfully!")

    except Exception as e:
        print(f"An error occurred: {e}")
        page.screenshot(path="jules-scratch/verification/error.png")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
