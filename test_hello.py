import hello

def test_greet():
    assert hello.greet("Alice") == "Hello, Alice!"
    assert hello.greet("Bob") == "Hello, Bob!"
