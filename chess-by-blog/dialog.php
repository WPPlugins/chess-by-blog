<?php
error_reporting(E_ALL);
session_start();
if (substr(__FILE__, -38) == "chess-by-blog/chess-by-blog/dialog.php") {
    require_once('../../../../wp-config.php');
    require_once('../../../../wp-admin/admin.php');
    require_once('../../../../wp-includes/functions.php');
} else {
    require_once('../../../wp-config.php');
    require_once('../../../wp-admin/admin.php');
    require_once('../../../wp-includes/functions.php');
}
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Chess setup</title>
<?php
wp_admin_css( 'css/global' );
wp_admin_css();
wp_admin_css( 'css/colors' );
?>
    <script language="JavaScript" type="text/javascript">
      var pgn = "";
      function addTag(label, s) {
	  pgn += '[' + label + ' &#34;' + s + '&#34;]\r\n';
      }
      function show() {
	  var mainform = document.getElementById("mainform");
	  addTag("Event", mainform.event.value);
	  addTag("Site", mainform.city.value + (mainform.city.value != "" ? ", " : "") + mainform.region.value + " " + mainform.countrycode.value);
	  addTag("Date", mainform.date.value);
	  addTag("Round", mainform.round.value);
	  addTag("White", mainform.whitelast.value + (mainform.whitelast.value != "" ? ", " : "") + mainform.whitefirst.value);
	  addTag("Black", mainform.blacklast.value + (mainform.blacklast.value != "" ? ", " : "") + mainform.blackfirst.value);
	  if (mainform.whiteid.value != "") {
	      addTag("CBBWhiteId", mainform.whiteid.value);
	  }
	  if (mainform.blackid.value != "") {
	      addTag("CBBBlackId", mainform.blackid.value);
	  }
	  addTag("Result", mainform.result.value);
	  if (mainform.fen.value != "") {
	      addTag("Setup", "1");
	      addTag("FEN", mainform.fen.value);
	  }
	  if (mainform.movelist.value != "") {
              pgn = pgn + '\n' + mainform.movelist.value;
          }
	  if (opener != null) {
	      opener.cbb_insert_text(pgn);
	      self.close();
	  } else {
	      top.cbb_insert_text(pgn);
	      top.tb_remove();
	  }
      }
<?php
      if (function_exists("get_users_of_blog")) {
          echo "var cbbUserIDs = new Array();\n";          
          echo "var cbbFirstNames = new Array();\n";
          echo "var cbbLastNames = new Array();\n";
          $users = get_users_of_blog();
          echo 'cbbUserIDs[0] = "*";';
          echo 'cbbFirstNames[0] = "";';
          echo 'cbbLastNames[0] = "";';
          $index = 1;
          foreach ($users as $user) {
              $wpuser = new WP_User($user->user_login);
              echo 'cbbUserIDs['. $index . '] = "' . $user->user_login . "\";\n";
	      if ($wpuser->display_name == $user->user_login) {
		  echo 'cbbFirstNames['. $index . '] = "' . "\";\n";
		  echo 'cbbLastNames['. $index . '] = "' . $wpuser->display_name . "\";\n";
	      } else if ($wpuser->display_name == $wpuser->first_name) {
		  echo 'cbbFirstNames['. $index . '] = "' . "\";\n";
		  echo 'cbbLastNames['. $index . '] = "' . $wpuser->first_name . "\";\n";
	      } else {
		  echo 'cbbFirstNames['. $index . '] = "' . $wpuser->first_name . "\";\n";
		  echo 'cbbLastNames['. $index . '] = "' . $wpuser->last_name . "\";\n";
	      }
              $index++;
          }
      }
?>

    </script>
  </head>
  <body>
    <div class="wrap">
      <form action="" onsubmit="javascript:show()" id="mainform">
      <p>
          <label for="cbb_event" title="<?php echo __('Name of tournament or match event', 'cbb');?>"><?php echo __('Event', 'cbb');?>:</label>    
          <input id="cbb_event" type="text" name="event" />
      </p>
      <fieldset>
          <legend> <?php echo __('Site', 'cbb');?> </legend>
          <p>
              <label for="cbb_city" title="<?php echo __('City in which match took place', 'cbb');?>"><?php echo __('City', 'cbb');?>:</label>
              <input id="cbb_city" type="text" name="city" value="<?php echo(get_option('blogname')); ?>"/>
              <label for="cbb_region" title="<?php echo __('Region in which match took place', 'cbb');?>"><?php echo __('Region', 'cbb');?>:</label>
	      <input id="cbb_region" type="text" size="8" name="region" />
          </p>
          <p>
              <label for="cbb_countrycode"><?php echo __('Country', 'cbb');?>:</label>          
	      <select id="cbb_countrycode" name="countrycode">
		<option value="AFG">Afghanistan</option>
		<option value="AIR">Aboard aircraft</option>
		<option value="ALB">Albania</option>
		<option value="ALG">Algeria</option>
		<option value="AND">Andorra</option>
		<option value="ANG">Angola</option>
		<option value="ANT">Antigua</option>
		<option value="ARG">Argentina</option>
		<option value="ARM">Armenia</option>
		<option value="ATA">Antarctica</option>
		<option value="AUS">Australia</option>
		<option value="AZB">Azerbaijan</option>
		<option value="BAN">Bangladesh</option>
		<option value="BAR">Bahrain</option>
		<option value="BHM">Bahamas</option>
		<option value="BEL">Belgium</option>
		<option value="BER">Bermuda</option>
		<option value="BIH">Bosnia and Herzegovina</option>
		<option value="BLA">Belarus</option>
		<option value="BLG">Bulgaria</option>
		<option value="BLZ">Belize</option>
		<option value="BOL">Bolivia</option>
		<option value="BRB">Barbados</option>
		<option value="BRS">Brazil</option>
		<option value="BRU">Brunei</option>
		<option value="BSW">Botswana</option>
		<option value="CAN">Canada</option>
		<option value="CHI">Chile</option>
		<option value="COL">Columbia</option>
		<option value="CRA">Costa Rica</option>
		<option value="CRO">Croatia</option>
		<option value="CSR">Czechoslovakia</option>
		<option value="CUB">Cuba</option>
		<option value="CYP">Cyprus</option>
		<option value="DEN">Denmark</option>
		<option value="DOM">Dominican Republic</option>
		<option value="ECU">Ecuador</option>
		<option value="EGY">Egypt</option>
		<option value="ENG">England</option>
		<option value="ESP">Spain</option>
		<option value="EST">Estonia</option>
		<option value="FAI">Faroe Islands</option>
		<option value="FIJ">Fiji</option>
		<option value="FIN">Finland</option>
		<option value="FRA">France</option>
		<option value="GAM">Gambia</option>
		<option value="GCI">Guernsey-Jersey</option>
		<option value="GEO">Georgia</option>
		<option value="GER">Germany</option>
		<option value="GHA">Ghana</option>
		<option value="GRC">Greece</option>
		<option value="GUA">Guatemala</option>
		<option value="GUY">Guyana</option>
		<option value="HAI">Haiti</option>
		<option value="HKG">Hong Kong</option>
		<option value="HON">Honduras</option>
		<option value="HUN">Hungary</option>
		<option value="IND">India</option>
		<option value="IRL">Ireland</option>
		<option value="IRN">Iran</option>
		<option value="IRQ">Iraq</option>
		<option value="ISD">Iceland</option>
		<option value="ISR">Israel</option>
		<option value="ITA">Italy</option>
		<option value="IVO">Ivory Coast</option>
		<option value="JAM">Jamaica</option>
		<option value="JAP">Japan</option>
		<option value="JRD">Jordan</option>
		<option value="JUG">Yugoslavia</option>
		<option value="KAZ">Kazakhstan</option>
		<option value="KEN">Kenya</option>
		<option value="KIR">Kyrgyzstan</option>
		<option value="KUW">Kuwait</option>
		<option value="LAT">Latvia</option>
		<option value="LEB">Lebanon</option>
		<option value="LIB">Libya</option>
		<option value="LIC">Liechtenstein</option>
		<option value="LTU">Lithuania</option>
		<option value="LUX">Luxembourg</option>
		<option value="MAL">Malaysia</option>
		<option value="MAU">Mauritania</option>
		<option value="MEX">Mexico</option>
		<option value="MLI">Mali</option>
		<option value="MLT">Malta</option>
		<option value="MNC">Monaco</option>
		<option value="MOL">Moldova</option>
		<option value="MON">Mongolia</option>
		<option value="MOZ">Mozambique</option>
		<option value="MRC">Morocco</option>
		<option value="MRT">Mauritius</option>
		<option value="MYN">Myanmar</option>
		<option value="NCG">Nicaragua</option>
		<option selected value="NET">The Internet</option>
		<option value="NIG">Nigeria</option>
		<option value="NLA">Netherlands Antilles</option>
		<option value="NLD">Netherlands</option>
		<option value="NOR">Norway</option>
		<option value="NZD">New Zealand</option>
		<option value="OST">Austria</option>
		<option value="PAK">Pakistan</option>
		<option value="PAL">Palestine</option>
		<option value="PAN">Panama</option>
		<option value="PAR">Paraguay</option>
		<option value="PER">Peru</option>
		<option value="PHI">Philippines</option>
		<option value="PNG">Papua New Guinea</option>
		<option value="POL">Poland</option>
		<option value="POR">Portugal</option>
		<option value="PRC">People&#39;s Republic of China</option>
		<option value="PRO">Puerto Rico</option>
		<option value="QTR">Qatar</option>
		<option value="RIN">Indonesia</option>
		<option value="ROM">Romania</option>
		<option value="RUS">Russia</option>
		<option value="SAF">South Africa</option>
		<option value="SAL">El Salvador</option>
		<option value="SCO">Scotland</option>
		<option value="SEA">At Sea</option>
		<option value="SEN">Senegal</option>
		<option value="SEY">Seychelles</option>
		<option value="SIP">Singapore</option>
		<option value="SLV">Slovenia</option>
		<option value="SMA">San Marino</option>
		<option value="SPC">Aboard spacecraft</option>
		<option value="SRI">Sri Lanka</option>
		<option value="SUD">Sudan</option>
		<option value="SUR">Surinam</option>
		<option value="SVE">Sweden</option>
		<option value="SWZ">Switzerland</option>
		<option value="SYR">Syria</option>
		<option value="TAI">Thailand</option>
		<option value="TMT">Turkmenistan</option>
		<option value="TRK">Turkey</option>
		<option value="TTO">Trinidad and Tobago</option>
		<option value="TUN">Tunisia</option>
		<option value="UAE">United Arab Emirates</option>
		<option value="UGA">Uganda</option>
		<option value="UKR">Ukraine</option>
		<option value="UNK">Unknown</option>
		<option value="URU">Uruguay</option>
		<option value="USA">United States of America</option>
		<option value="UZB">Uzbekistan</option>
		<option value="VEN">Venezuela</option>
		<option value="VGB">British Virgin Islands</option>
		<option value="VIE">Vietnam</option>
		<option value="VUS">U.S. Virgin Islands</option>
		<option value="WLS">Wales</option>
		<option value="YEM">Yemen</option>
		<option value="YUG">Yugoslavia</option>
		<option value="ZAM">Zambia</option>
		<option value="ZIM">Zimbabwe</option>
		<option value="ZRE">Zaire</option>
	      </select>
          </p>
      </fieldset>
      <fieldset>
          <legend> <?php echo __('Players', 'cbb');?> </legend>
            <label for="cbb_whitewordpressid" title="<?php echo __('WordPress user who will play white', 'cbb');?>"><?php echo __('White', 'cbb');?>:</label>
<?php
            if (function_exists("get_users_of_blog")) {
?>
	        <select id="cbb_whitewordpressid" name="whiteid" onchange="cbb_whitefirst.value=cbbFirstNames[this.selectedIndex];cbb_whitelast.value=cbbLastNames[this.selectedIndex];">
                <option value="*">* (<?php echo __('Anyone', 'cbb');?>)</option>
                    <script>
                     for (var i = 1; i < cbbUserIDs.length; ++i) {
                         var $name = cbbUserIDs[i];
                         if (cbbFirstNames[i] != "" && cbbLastNames[i] != "") {
                             $name = cbbFirstNames[i] + ' ' + cbbLastNames[i] + " (" + cbbUserIDs[i] + ')';
                         } else if (cbbLastNames[i] != "" && cbbLastNames[i] != cbbUserIDs[i]) {
			     $name = cbbLastNames[i] + " (" + cbbUserIDs[i] + ')';
			 }
                         document.write('<option value="' + cbbUserIDs[i] + '">' + $name + '</option>');
                     }
                </script>
                </select>
<?php
            } else {
                echo '<input id="cbb_whitewordpressid" type="text" size="12" name="whiteid" />';
            }
?>
          <input type = "hidden" id="cbb_whitefirst" type="text" size="12" name="whitefirst" />
	  <input type = "hidden" id="cbb_whitelast" type="text" size="12" name="whitelast" />
            <label for="cbb_blackwordpressid" title="<?php echo __('WordPress user who will play black', 'cbb');?>"><?php echo __('Black', 'cbb');?>:</label>
<?php
            if (function_exists("get_users_of_blog")) {
?>
	        <select id="cbb_blackwordpressid" name="blackid" onchange="cbb_blackfirst.value=cbbFirstNames[this.selectedIndex];cbb_blacklast.value=cbbLastNames[this.selectedIndex];">
                <option value="*">* (<?php echo __('Anyone', 'cbb');?>)</option>
                <script>
                     for (var i = 1; i < cbbUserIDs.length; ++i) {
                         var $name = cbbUserIDs[i];
                         if (cbbFirstNames[i] != "" && cbbLastNames[i] != "") {
                             $name = cbbFirstNames[i] + ' ' + cbbLastNames[i] + " (" + cbbUserIDs[i] + ')';
                         } else if (cbbFirstNames[i] != "" && cbbFirstNames[i] != cbbUserIDs[i]) {
			     $name = cbbFirstNames[i] + " (" + cbbUserIDs[i] + ')';
			 }
                         document.write('<option value="' + cbbUserIDs[i] + '">' + $name + '</option>');
                     }
                </script>
                </select>
<?php
            } else {
                echo '<input id="cbb_blackwordpressid" type="text" size="12" name="blackid" />';
            }
?>
	  <input type="hidden" id="cbb_blackfirst" type="text" size="12" name="blackfirst" />
	  <input type="hidden" id="cbb_blacklast" type="text" size="12" name="blacklast" />
      </fieldset>
      <p>
          <label for="cbb_date" title="<?php echo __('Starting date of the game, in YYYY.MM.DD format', 'cbb');?>"><?php echo __('Date', 'cbb');?>:</label>
          <input id="cbb_date" type="text" name="date" value="<?php echo date("Y.m.d"); ?>"/>
          <label for="cbb_round" title="<?php echo __('The playing round ordinal of the game within the event', 'cbb');?>"><?php echo __('Round', 'cbb');?>:</label>
          <input id="cbb_round" type="text" name="round" />
      </p>
      <fieldset>
          <legend> <?php echo __('Game status', 'cbb');?> </legend>
          <p>
            <label for="cbb_result"><?php echo __('Result', 'cbb');?>:</label>
	    <select id="cbb_result" name="result">
	      <option selected value="*">Ongoing</option>
	      <option value="1-0">White Won</option>
	      <option value="0-1">Black Won</option>
	      <option value="1/2-1/2">Draw</option>
            </select>
          </p>
          <p>
            <label for="cbb_fen" title="<?php echo __('Initial position of game, in Forsyth-Edwards Notation', 'cbb');?>"><?php echo __('FEN', 'cbb');?>:</label>
	    <input id="cbb_fen" type="text" name="fen" size="40" />
          </p>
          <p>
            <label for="cbb_movelist" title="<?php echo __('Moves of the game so far, in Standard Algebraic Notation', 'cbb');?>"><?php echo __('Movelist', 'cbb');?>:</label>
          </p>
	  <textarea id="cbb_movelist" name="movelist" style="width:100%" cols="60" rows="4"></textarea>
      </fieldset>
      <p class="submit"><input type="submit" value="<?php echo __('Insert into Post', 'cbb');?>" /></p>
    </form>
    </div>
  </body>
</html>
