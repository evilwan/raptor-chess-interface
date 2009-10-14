package raptor.speech;

public interface Speech {
   public void init();
   public void speak(String text);
   public void dispose();
}
